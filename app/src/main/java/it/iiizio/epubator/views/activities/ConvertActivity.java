/*
Copyright (C)2011 Ezio Querini <iiizio AT users.sf.net>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.iiizio.epubator.views.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.StringRes;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.iiizio.epubator.R;
import it.iiizio.epubator.model.constants.BundleKeys;
import it.iiizio.epubator.model.constants.ConversionStatus;
import it.iiizio.epubator.model.constants.DecissionOnConversionError;
import it.iiizio.epubator.model.constants.PreferencesKeys;
import it.iiizio.epubator.model.exceptions.ConversionException;
import it.iiizio.epubator.model.utils.FileHelper;
import it.iiizio.epubator.model.utils.HtmlHelper;
import it.iiizio.epubator.model.utils.PdfReadHelper;
import it.iiizio.epubator.presenters.ConvertPresenter;
import it.iiizio.epubator.presenters.ConvertPresenterImpl;
import it.iiizio.epubator.views.dto.ConversionPreferences;
import it.iiizio.epubator.views.dto.ConversionSettings;
import it.iiizio.epubator.views.events.ConversionFinishedEvent;
import it.iiizio.epubator.views.events.ProgressUpdateEvent;
import it.iiizio.epubator.views.utils.BundleHelper;
import it.iiizio.epubator.views.utils.PreferencesHelper;

public class ConvertActivity extends Activity implements ConvertView {

	private static ScrollView sv_progress;
	private static TextView tv_progress;
	private static Button bt_ok;
	private static Button bt_stopConversion;

	private static boolean conversionInProgress = false;
	private static int result;

	private ConversionPreferences preferences;
	private ConversionSettings settings;

	private ConvertPresenter presenter;
	private ConvertTask convertTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.progressview);

		presenter = new ConvertPresenterImpl(this);

		sv_progress = (ScrollView)findViewById(R.id.scroll);
		tv_progress = (TextView) findViewById(R.id.progress);
		setupButtons();

		getPrefs();

		if (conversionInProgress) {
			updateProgressText("");
			setButtons();
			return;
		}

		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			Toast.makeText(this, getString(R.string.file_not_found), Toast.LENGTH_SHORT).show();
			return;
		}

		String pdfFilename = BundleHelper.getExtraStringOrDefault(extras, BundleKeys.FILENAME);
		if(pdfFilename==null){
			Toast.makeText(this, getString(R.string.file_not_found), Toast.LENGTH_SHORT).show();
			return;
		}

		String coverFile = BundleHelper.getExtraStringOrEmpty(extras, BundleKeys.COVER);
		String temporalPath = getExternalCacheDir() + "/";
		settings = new ConversionSettings(preferences, pdfFilename, temporalPath, getDownloadDirectory(), coverFile);

		startConversion(settings);
	}

    @Override
    public void onResume() {
        super.onResume();
		EventBus.getDefault().register(this);
        getPrefs();
    }

	@Override
	protected void onPause() {
		super.onPause();
		EventBus.getDefault().unregister(this);
	}

	@Override
    public void onBackPressed() {
        finish();
    }

	/**
	 * Ask user what to do with the converted file after there has been any error
	 */
	private void handleConvertedFileAfterError(){
		new AlertDialog.Builder(ConvertActivity.this)
			.setTitle(getResources().getString(R.string.extraction_error))
			.setMessage(getResources().getString(R.string.keep_epub_file))
			.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					keepEpub();
				}
			})
			.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					updateProgressText("\n" + getResources().getStringArray(R.array.conversion_result_message)[4] + "\n");
					deleteTemporalFile();
				}
			})
			.setNeutralButton(getResources().getString(R.string.verify_epub), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					Intent verify = new Intent(getBaseContext(), VerifyActivity.class);
					verify.putExtra(BundleKeys.FILENAME, settings.tempFilename);
					startActivityForResult(verify, 0);
				}
			}).create()
			.show();
	}

	private String getDownloadDirectory(){
		return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/";
	}

	private void setupButtons() {
		bt_ok = (Button)findViewById(R.id.ok);
		bt_stopConversion = (Button)findViewById(R.id.stop);

		bt_ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				conversionInProgress = false;
				finish();
			}
		});
		bt_stopConversion.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				result = ConversionStatus.CONVERSION_STOPPED_BY_USER;
				convertTask.cancel(true);
				sendFinishNotification();
				conversionInProgress = false;
			}
		});
	}

	private void getPrefs() {
		PreferencesHelper prefs = new PreferencesHelper(this);

		preferences = new ConversionPreferences();
		preferences.includeImages = prefs.getBoolean(PreferencesKeys.ADD_EXTRACTED_IMAGES_FROM_PDF, true);
		preferences.repeatedImages = prefs.getBoolean(PreferencesKeys.ADD_REPEATED_IMAGES);
		preferences.pagesPerFile = prefs.getParsedString(PreferencesKeys.PAGES_PER_FILE, 5);
		preferences.onError = prefs.getParsedString(PreferencesKeys.OPTION_WHEN_ERROR_IN_CONVERSION, DecissionOnConversionError.KEEP_ITEM);
		preferences.addMarkers = prefs.getBoolean(PreferencesKeys.MARK_ERRORS, true);
		preferences.tocFromPdf = prefs.getBoolean(PreferencesKeys.TRY_TO_EXTRACT_TOC_FROM_PDF, true);
		preferences.showLogoOnCover = prefs.getBoolean(PreferencesKeys.HAVE_LOGO_ON_COVER, true);
		preferences.saveOnDownloadDirectory = prefs.getBoolean(PreferencesKeys.SAVE_ALWAYS_ON_DOWNLOAD_DIRECTORY);
	}

	private void setButtons() {
		bt_ok.setEnabled(!conversionInProgress); // Ok button only enabled before or after conversion
		bt_stopConversion.setEnabled(conversionInProgress); // Stop button only enabled during conversion
	}

	private void deleteTemporalFile() {
		new File(settings.tempFilename).delete();
		if (new File(settings.oldFilename).exists()) {
			new File(settings.oldFilename).renameTo(new File(settings.epubFilename));
			updateProgressText(getResources().getString(R.string.kept_old_epub));
		} else {
			updateProgressText(getResources().getString(R.string.epub_was_deleted));
		}
	}

	private void keepEpub() {
		updateProgressText("\n" + getResources().getStringArray(R.array.conversion_result_message)[0] + "\n");
		if (preferences.addMarkers) {
			String pageNumberString = getResources().getString(R.string.pagenumber, ">>\n");
			updateProgressText(getResources().getString(R.string.errors_are_marked_with, "<<@") + pageNumberString);
			updateProgressText(getResources().getString(R.string.lost_pages_are_marked_with, "<<#") + pageNumberString);
		}
		renameFile();
		updateProgressText(getResources().getString(R.string.epubfile, settings.epubFilename));
	}

	private void renameFile() {
		new File(settings.tempFilename).renameTo(new File(settings.epubFilename));
		new File(settings.oldFilename).delete();
	}

    private void startConversion(ConversionSettings settings){
		sendStartNotification();
		result = ConversionStatus.SUCCESS;

		conversionInProgress = true;
		setButtons();

		convertTask = new ConvertTask(settings, preferences);
		convertTask.execute();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onConversionFinished(ConversionFinishedEvent event){
		conversionInProgress = false;
		result = event.getResult();
		sendFinishNotification();
		setButtons();
	}

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onProgressUpdate(ProgressUpdateEvent event){
		updateProgressText(event.getMessage());
	}

	private void updateProgressText(String text){
		tv_progress.setText(text);
		scrollUp();
	}

	private void scrollUp() {
		sv_progress.post(new Runnable() {
			public void run() {
				sv_progress.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});
	}

	private void sendStartNotification(){
		sendNotification(getResources().getString(R.string.conversion_in_progress), true);
	}

	private void sendFinishNotification(){
		sendNotification(getResources().getStringArray(R.array.conversion_result_message)[result], false);
	}

	private void sendNotification(String statusTitle, boolean fixed) {
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, ConvertActivity.class), 0);

		Notification notification = new NotificationCompat.Builder(this)
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentTitle(statusTitle)
			.setContentText(settings.filename)
			.setStyle(new NotificationCompat.BigTextStyle().bigText(settings.filename))
			.setContentIntent(contentIntent)
			.setOngoing(fixed)
			.setAutoCancel(!fixed)
			.build();

		getNotificationManager().notify(R.string.app_name, notification);
	}

	private NotificationManager getNotificationManager(){
		return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	}

	@Override
	public void noTocFoundInThePdfFile() {
		publishProgressMessage(getResources().getString(R.string.no_toc_found));
	}

	@Override
	public void createdDummyToc() {
		publishProgressMessage(getResources().getString(R.string.create_dummy_toc));
	}

	@Override
	public void tocExtractedFromPdfFile() {
		publishProgressMessage(getResources().getString(R.string.toc_extracted_from_pdf));
	}

	@Override
	public void coverWithImageCreated() {
		publishProgressMessage(getResources().getString(R.string.create_cover_with_image));
	}

	@Override
	public void coverWithTitleCreated() {
		publishProgressMessage(getResources().getString(R.string.create_cover_with_title));
	}

	@Override
	public Bitmap getAppLogo() {
		return BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
	}

	private void publishProgressMessage(String message){
		if(convertTask!=null && !convertTask.isCancelled()){
			convertTask.publishProgressMessage(message);
		}
	}

	private class ConvertTask extends AsyncTask<Void, String, Integer> {

		private final StringBuilder progressSb;
		private final ConversionSettings settings;
		private final ConversionPreferences preferences;

		public ConvertTask(ConversionSettings settings, ConversionPreferences preferences) {
			progressSb = new StringBuilder();
			this.settings = settings;
			this.preferences = preferences;
		}

		public void publishProgressMessage(String message) {
			publishProgress(message);
		}

		@Override
		protected void onPreExecute() {
			publishProgress(R.string.heading);
			publishProgress(R.string.pdf_extraction_library);
		}

		private void publishProgress(@StringRes int text){
			publishProgress(getResources().getString(text));
		}

		@Override
		protected void onProgressUpdate(String... messageArray) {
			String message = TextUtils.join("\n", messageArray) + "\n";
			progressSb.append(message);
			EventBus.getDefault().post(new ProgressUpdateEvent(progressSb.toString()));
		}

		@Override
		protected Integer doInBackground(Void... params) {
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
				renameFile();
				publishProgress(getResources().getString(R.string.epubfile, settings.epubFilename));
			} else if (result == ConversionStatus.EXTRACTION_ERROR) {
				if (preferences.onError == DecissionOnConversionError.KEEP_ITEM) {
					keepEpub();
					result = ConversionStatus.SUCCESS;
				} else if (preferences.onError == DecissionOnConversionError.DISCARD_ITEM){
					deleteTemporalFile();
				} else {
					if (hasWindowFocus()) {
						handleConvertedFileAfterError();
					} else {
						// In case of doubt, we keep the epub
						keepEpub();
						result = ConversionStatus.SUCCESS;
					}
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

			String title = settings.filename.replaceAll("[^\\p{Alnum}]", " ");
			String bookId = title + " - " + new Date().hashCode();

			publishProgress(getResources().getString(R.string.toc));
			presenter.addToc(pages, bookId, title, preferences.tocFromPdf, pagesPerFile);

			publishProgress(getResources().getString(R.string.frontpage));
			presenter.addFrontPage();

			publishProgress(getResources().getString(R.string.frontpagepng));
			presenter.addFrontpageCover(settings.filename, settings.coverFile, preferences.showLogoOnCover);

			boolean hasPartialExtractionError = false;

			// Add extracted text and images
			List<String> allImageList = new ArrayList<>();
			for(int i = 1; i <= pages; i += pagesPerFile) {
				StringBuilder pageText = new StringBuilder();

				publishProgress(getResources().getString(R.string.html, i));
				int endPage = Math.min(i + pagesPerFile - 1, pages);

				for (int j = i; j <= endPage; j++) {
					// Add anchor
					pageText.append("  <p>\n");
					pageText.append("  <a id=\"page" + j + "\"/>\n");

					// extract text
					String page = HtmlHelper.stringToHTMLString(PdfReadHelper.getPageText(j));
					if (page.length() == 0) {
						publishProgress(getResources().getString(R.string.extraction_failure, j));
						hasPartialExtractionError = true;
						if (preferences.addMarkers) {
							pageText.append("&lt;&lt;#" + j + "&gt;&gt;");
						}
					} else {
						if (page.matches(".*\\p{Cntrl}.*")) {
							hasPartialExtractionError = true;
							if (preferences.addMarkers) {
								pageText.append(page.replaceAll("\\p{Cntrl}+", "&lt;&lt;@" + j + "&gt;&gt;"));
							} else {
								pageText.append(page.replaceAll("\\p{Cntrl}+", " "));
							}
						} else {
							pageText.append(page);
						}
					}

					if (preferences.includeImages) {
						List<String> imageList = PdfReadHelper.getPageImages(j);
						for (String imageName: imageList){
							String imageTag = "\n<img alt=\"" + imageName + "\" src=\"" + imageName + "\" /><br/>";

							if (!allImageList.contains(imageName)) {
								allImageList.add(imageName);
								publishProgress(getResources().getString(R.string.image_added, imageName));
								pageText.append(imageTag);
							} else if (preferences.repeatedImages) {
								pageText.append(imageTag);
							}
						}
					}
					// Close page
					pageText.append("\n  </p>\n");
				}

				presenter.addPage(i, pageText.toString());
			}

			publishProgress(getResources().getString(R.string.content));
			presenter.addContent(pages, bookId, title, allImageList, pagesPerFile);

			publishProgress(getResources().getString(R.string.close_file));
			presenter.closeFile(settings.tempFilename);

			if (hasPartialExtractionError) {
				throw new ConversionException(ConversionStatus.EXTRACTION_ERROR);
			}
		}
	}
}
