package it.iiizio.epubator.infrastructure.services;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import it.iiizio.epubator.R;
import it.iiizio.epubator.domain.callbacks.PageBuildEvents;
import it.iiizio.epubator.domain.constants.BundleKeys;
import it.iiizio.epubator.domain.constants.ConversionStatus;
import it.iiizio.epubator.domain.constants.DecisionOnConversionError;
import it.iiizio.epubator.domain.entities.ConversionSettings;
import it.iiizio.epubator.domain.entities.PdfExtraction;
import it.iiizio.epubator.domain.exceptions.ConversionException;
import it.iiizio.epubator.domain.services.PdfReaderServiceImpl;
import it.iiizio.epubator.domain.services.ZipWriterServiceImpl;
import it.iiizio.epubator.domain.utils.PdfXmlParserImpl;
import it.iiizio.epubator.infrastructure.providers.FileProviderImpl;
import it.iiizio.epubator.infrastructure.providers.ImageProvider;
import it.iiizio.epubator.infrastructure.providers.ImageProviderImpl;
import it.iiizio.epubator.presentation.events.ConversionCanceledEvent;
import it.iiizio.epubator.presentation.events.ConversionFinishedEvent;
import it.iiizio.epubator.presentation.events.ConversionStatusChangedEvent;
import it.iiizio.epubator.presentation.events.ProgressUpdateEvent;
import it.iiizio.epubator.presentation.utils.NotificationHelper;
import it.iiizio.epubator.presentation.views.activities.ConvertActivity;

public class ConversionService extends Service implements PageBuildEvents {

    //<editor-fold desc="Attributes">
    private ConversionManager manager;
    private ConversionTask conversionTask;
    private String currentFile;
    //</editor-fold>

    //<editor-fold desc="Methods">
    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		this.manager = makeManager();

        Bundle extras = intent.getExtras();
		ConversionSettings settings = (ConversionSettings) extras.getSerializable(BundleKeys.CONVERSION_SETTINGS);
        currentFile = settings.filename;
		conversionTask = new ConversionTask(settings);
        conversionTask.execute();

        startForeground(startId, makeStartNotification());

        return super.onStartCommand(intent, flags, startId);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConversionCanceled(ConversionCanceledEvent event){
    	String progress = event.getProgress();
        if(conversionTask!=null){
			progress = conversionTask.getProgress();
			conversionTask.cancel(true);
        }
		finishConversion(event.getResult(), progress);
    }
    //</editor-fold>

    //<editor-fold desc="Page build events">
    @Override
    public void pageAdded(int page) {
        String pageAddedMessage = "\t>> " + getResources().getString(R.string.page_added, page);
        publishProgress(pageAddedMessage);
    }

    @Override
    public void pageFailure(int j) {
        publishProgress(getResources().getString(R.string.extraction_failure, j));
    }

    @Override
    public void imageAdded(String imageName) {
        String imageAddedMessage = "\t\t>>>> " + getResources().getString(R.string.image_added, imageName);
        publishProgress(imageAddedMessage);
    }

    @Override
    public void noTocFoundInThePdfFile() {
        publishProgress(getResources().getString(R.string.no_toc_found));
    }

    @Override
    public void dummyTocCreated() {
        publishProgress(getResources().getString(R.string.create_dummy_toc));
    }

    @Override
    public void tocExtractedFromPdfFile() {
        publishProgress(getResources().getString(R.string.toc_extracted_from_pdf));
    }

    @Override
    public void coverWithImageCreated() {
        publishProgress(getResources().getString(R.string.create_cover_with_image));
    }

    @Override
    public void coverWithTitleCreated() {
        publishProgress(getResources().getString(R.string.create_cover_with_title));
    }

	@Override
	public String getLocaleLanguage() {
		return Resources.getSystem().getConfiguration().locale.getLanguage();
	}
	//</editor-fold>

    //<editor-fold desc="Private methods">
    private ConversionManager makeManager(){
		ImageProvider imageProvider = new ImageProviderImpl(getApplicationContext());
		return new ConversionManagerImpl(this, imageProvider, new FileProviderImpl(),
			new PdfReaderServiceImpl(), new ZipWriterServiceImpl(), new PdfXmlParserImpl());
	}

    private void publishProgress(String message){
        if(conversionTask!=null){
            conversionTask.makeProgress(message);
        }
    }

    private Notification makeStartNotification(){
        return makeNotification(getResources().getString(R.string.conversion_in_progress));
    }

    private void finishConversion(int result, String progress){
		stopForeground(false);
		sendFinishNotification(result, progress);
		stopSelf();
	}

    private void sendFinishNotification(int result, String progress){
		Intent openConvertActivityIntent = gotoConversionViewIntent();
		openConvertActivityIntent.putExtra(BundleKeys.CONVERSION_TEXT, progress);
		String resultMessage = getResources().getStringArray(R.array.conversion_result_message)[result];
		Notification notification = makeNotification(resultMessage, false, openConvertActivityIntent);
        notification.defaults |= Notification.DEFAULT_VIBRATE;
		NotificationHelper.sendNotification(this, R.string.app_name, notification);
    }

    private Notification makeNotification(String statusTitle) {
		return makeNotification(statusTitle, true, gotoConversionViewIntent());
    }

    private Intent gotoConversionViewIntent(){
    	return new Intent(this, ConvertActivity.class)
			.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	}

	private Notification makeNotification(String statusTitle, boolean fixed,
			  Intent openConvertActivityIntent) {
		return NotificationHelper.makeNotification(this,
				statusTitle, this.currentFile, fixed, openConvertActivityIntent);
	}
    //</editor-fold>

    //<editor-fold desc="Inner classes">
    private class ConversionTask extends AsyncTask<Void, String, Integer> {

        private final StringBuilder progressSb;
    	private final ConversionSettings settings;

		private ConversionTask(ConversionSettings settings) {
			this.settings = settings;
			this.progressSb = new StringBuilder();
		}

		private void makeProgress(String message){
            publishProgress(message);
        }

        private String getProgress(){
			return progressSb.toString();
		}

        @Override
        protected void onProgressUpdate(String... messageArray) {
            String message = TextUtils.join("\n", messageArray) + "\n";
            progressSb.append(message);
            EventBus.getDefault().postSticky(new ProgressUpdateEvent(getProgress()));
        }

        @Override
        protected Integer doInBackground(Void... voids) {
			manager.removeCacheFiles(settings);
			manager.saveOldEpub(settings);

            try {
                EventBus.getDefault().postSticky(new ConversionStatusChangedEvent(ConversionStatus.LOADING_FILE));
                manager.loadPdfFile(settings.pdfFilename);
                EventBus.getDefault().postSticky(new ConversionStatusChangedEvent(ConversionStatus.IN_PROGRESS));
                fillEpub(settings.getPreferences().pagesPerFile);
                return ConversionStatus.SUCCESS;
            } catch (ConversionException ex){
                return ex.getStatus();
            } catch (OutOfMemoryError ex){
                return ConversionStatus.OUT_OF_MEMORY_ERROR;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            EventBus.getDefault().postSticky(new ConversionStatusChangedEvent(result));
            if(result == ConversionStatus.SUCCESS){
                manager.saveEpub(settings);
            } else if (result == ConversionStatus.EXTRACTION_ERROR) {
                if (settings.getPreferences().onError == DecisionOnConversionError.KEEP_ITEM) {
                    manager.saveEpub(settings);
                } else if (settings.getPreferences().onError == DecisionOnConversionError.DISCARD_ITEM){
                    deleteTemporalFile();
                } else {
                    // TODO: Update the notification, to request action when tapped
                    EventBus.getDefault().post(new ConversionFinishedEvent(result, settings, true));
                    return;
                }
            } else {
                deleteTemporalFile();
            }
            EventBus.getDefault().post(new ConversionFinishedEvent(result, settings));
			finishConversion(result, getProgress());
        }

        private void fillEpub(int pagesPerFile) throws ConversionException, OutOfMemoryError {
            int pages = manager.getBookPages();
            publishProgress(getResources().getString(R.string.number_of_pages, pages));

            publishProgress(getResources().getString(R.string.create_epub));
            manager.openFile(settings.tempFilename);

            publishProgress(getResources().getString(R.string.mimetype));
            manager.addMimeType();

            publishProgress(getResources().getString(R.string.container));
            manager.addContainer();

            String title = settings.getTitle();
            String bookId = settings.generateBookId();

            publishProgress(getResources().getString(R.string.toc));
            manager.addToc(bookId, title, settings.getPreferences().tocFromPdf, pagesPerFile);

            publishProgress(getResources().getString(R.string.frontpage));
            manager.addFrontPage();

            publishProgress(getResources().getString(R.string.frontpagepng));
            manager.addFrontpageCover(settings.filename, settings.coverFile, settings.getPreferences().showLogoOnCover);

            PdfExtraction extraction = manager.addPages(settings.getPreferences());

            publishProgress(getResources().getString(R.string.content));
            manager.addContent(bookId, title, extraction.getPdfImages(), pagesPerFile);

            publishProgress(getResources().getString(R.string.close_file));
            manager.closeFile(settings.tempFilename);

            if (extraction.hadExtractionError()) {
                throw new ConversionException(ConversionStatus.EXTRACTION_ERROR);
            }
        }

        private void deleteTemporalFile() {
            boolean exists = manager.deleteTemporalFile(settings);
            if (exists) {
                publishProgress(getResources().getString(R.string.kept_old_epub));
            } else {
                publishProgress(getResources().getString(R.string.epub_was_deleted));
            }
        }
    }
    //</editor-fold>
}
