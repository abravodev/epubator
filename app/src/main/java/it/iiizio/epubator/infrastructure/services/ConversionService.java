package it.iiizio.epubator.infrastructure.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import it.iiizio.epubator.R;
import it.iiizio.epubator.domain.constants.BundleKeys;
import it.iiizio.epubator.domain.constants.ConversionStatus;
import it.iiizio.epubator.domain.constants.DecissionOnConversionError;
import it.iiizio.epubator.domain.exceptions.ConversionException;
import it.iiizio.epubator.domain.utils.FileHelper;
import it.iiizio.epubator.domain.utils.PdfReadHelper;
import it.iiizio.epubator.presentation.callbacks.PageBuildEvents;
import it.iiizio.epubator.presentation.dto.ConversionPreferences;
import it.iiizio.epubator.presentation.dto.ConversionSettings;
import it.iiizio.epubator.presentation.dto.PdfExtraction;
import it.iiizio.epubator.presentation.events.ConversionCanceledEvent;
import it.iiizio.epubator.presentation.events.ConversionFinishedEvent;
import it.iiizio.epubator.presentation.events.ProgressUpdateEvent;

public class ConversionService extends Service {

    private final StringBuilder progressSb;
    private ConversionSettings settings;
    private ConversionPreferences preferences;
    private final ConvertManager presenter;
    private ConversionTask conversionTask;

    public ConversionService() {
        super();
        this.progressSb = new StringBuilder();
        this.presenter = new ConvertManagerImpl(this.pageBuild);
    }

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
        Bundle extras = intent.getExtras();
        this.settings = (ConversionSettings) extras.getSerializable(BundleKeys.CONVERSION_PREFERENCES);
        this.preferences = this.settings.getPreferences();
        conversionTask = new ConversionTask();
        conversionTask.execute();

        return super.onStartCommand(intent, flags, startId);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConversionCanceled(ConversionCanceledEvent event){
        if(conversionTask!=null){
            conversionTask.cancel(true);
        }
        stopSelf();
    }

    private void publishProgress(String message){
        if(conversionTask!=null){
            conversionTask.makeProgress(message);
        }
    }

    private PageBuildEvents pageBuild = new PageBuildEvents() {
        @Override
        public void addPage(int page) {
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
        public void createdDummyToc() {
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
        public Bitmap getAppLogo() {
            return BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        }
    };

    private class ConversionTask extends AsyncTask<Void, String, Integer> {

        @Override
        protected void onPreExecute() {
            publishProgress(R.string.heading);
            publishProgress(R.string.pdf_extraction_library);
        }

        private void publishProgress(@StringRes int text){
            publishProgress(getResources().getString(text));
        }

        private void makeProgress(String message){
            publishProgress(message);
        }

        @Override
        protected void onProgressUpdate(String... messageArray) {
            String message = TextUtils.join("\n", messageArray) + "\n";
            progressSb.append(message);
            EventBus.getDefault().postSticky(new ProgressUpdateEvent(progressSb.toString()));
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            removeCacheFiles();
            saveOldEPUB();

            try {
                publishProgress(getResources().getString(R.string.load, settings.pdfFilename));
                presenter.loadPdfFile(settings.pdfFilename);
                fillEpub(preferences.pagesPerFile);
                return ConversionStatus.SUCCESS;
            } catch (ConversionException ex){
                return ex.getStatus();
            } catch (OutOfMemoryError ex){
                return ConversionStatus.OUT_OF_MEMORY_ERROR;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            publishProgress("\n" + getResources().getStringArray(R.array.conversion_result_message)[result]);

            if(result == ConversionStatus.SUCCESS){
                presenter.saveEpub(settings);
                publishProgress(getResources().getString(R.string.epubfile, settings.epubFilename));
            } else if (result == ConversionStatus.EXTRACTION_ERROR) {
                if (preferences.onError == DecissionOnConversionError.KEEP_ITEM) {
                    presenter.saveEpub(settings);
                } else if (preferences.onError == DecissionOnConversionError.DISCARD_ITEM){
                    deleteTemporalFile();
                } else {
                    // TODO: Update the notification, to request action when tapped
                    EventBus.getDefault().post(new ConversionFinishedEvent(result, true));
                    return;
                }
            } else {
                deleteTemporalFile();
            }
            EventBus.getDefault().post(new ConversionFinishedEvent(result));
        }

        private void saveOldEPUB() {
            if (new File(settings.epubFilename).exists()) {
                new File(settings.epubFilename).renameTo(new File(settings.oldFilename));
            }
        }

        private void removeCacheFiles() {
            FileHelper.deleteFilesFromDirectory(new File(settings.temporalPath));
        }

        private void fillEpub(int pagesPerFile) throws ConversionException, OutOfMemoryError {
            int pages = PdfReadHelper.getPages();
            publishProgress(getResources().getString(R.string.number_of_pages, pages));

            publishProgress(getResources().getString(R.string.create_epub));
            presenter.openFile(settings.tempFilename);

            publishProgress(getResources().getString(R.string.mimetype));
            presenter.addMimeType();

            publishProgress(getResources().getString(R.string.container));
            presenter.addContainer();

            String title = settings.getTitle();
            String bookId = settings.getBookId();

            publishProgress(getResources().getString(R.string.toc));
            presenter.addToc(pages, bookId, title, preferences.tocFromPdf, pagesPerFile);

            publishProgress(getResources().getString(R.string.frontpage));
            presenter.addFrontPage();

            publishProgress(getResources().getString(R.string.frontpagepng));
            presenter.addFrontpageCover(settings.filename, settings.coverFile, preferences.showLogoOnCover);

            PdfExtraction extraction = presenter.addPages(preferences, pages, pagesPerFile);

            publishProgress(getResources().getString(R.string.content));
            presenter.addContent(pages, bookId, title, extraction.getPdfImages(), pagesPerFile);

            publishProgress(getResources().getString(R.string.close_file));
            presenter.closeFile(settings.tempFilename);

            if (extraction.hadExtractionError()) {
                throw new ConversionException(ConversionStatus.EXTRACTION_ERROR);
            }
        }

        private void deleteTemporalFile() {
            boolean exists = presenter.deleteTemporalFile(settings);
            if (exists) {
                publishProgress(getResources().getString(R.string.kept_old_epub));
            } else {
                publishProgress(getResources().getString(R.string.epub_was_deleted));
            }
        }
    }
}
