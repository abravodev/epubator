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
import android.app.Dialog;
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
import it.iiizio.epubator.model.utils.FileHelper;
import it.iiizio.epubator.model.utils.HtmlHelper;
import it.iiizio.epubator.model.utils.PdfReadHelper;
import it.iiizio.epubator.model.utils.ZipWriter;
import it.iiizio.epubator.presenters.ConvertPresenter;
import it.iiizio.epubator.presenters.ConvertPresenterImpl;
import it.iiizio.epubator.views.events.ConversionFinishedEvent;
import it.iiizio.epubator.views.events.ProgressUpdateEvent;
import it.iiizio.epubator.views.utils.BundleHelper;
import it.iiizio.epubator.views.utils.PreferencesHelper;

public class ConvertActivity extends Activity implements ConvertView {

	private static ScrollView sv_progress;
	private static TextView tv_progress;
	private static Button bt_ok;
	private static Button bt_stop_conversion;

	private static boolean okBtEnabled = true;
	public static boolean conversionStarted = false;
	private static int result;
	private static String filename = "";
	private static String tempPath = "";
	private static String pdfFilename = "";
	private static String epubFilename = "";
	private static String oldFilename = "";
	private static String tempFilename = "";
	private static String cover_file = "";

	private boolean includeImages;
	private boolean repeatedImages;
	private int pagesPerFile;
	private int onError;
	private boolean addMarkers;
	private boolean tocFromPdf;
	private boolean showLogoOnCover;
	private boolean saveOnDownloadDirectory;

	private final String EPUB_EXT = " - ePUBator.epub";
	private final String OLD_EXT = " - ePUBator.old";
	private final String TEMP_EXT = " - ePUBator.tmp";

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

		if (conversionStarted) {
			updateProgressText("");
			setButtons(okBtEnabled);
			return;
		}

		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			Toast.makeText(this, getString(R.string.file_not_found), Toast.LENGTH_SHORT).show();
			return;
		}

		cover_file = BundleHelper.getExtraStringOrEmpty(extras, BundleKeys.COVER);
		pdfFilename = BundleHelper.getExtraStringOrDefault(extras, BundleKeys.FILENAME);
		if(pdfFilename==null){
			Toast.makeText(this, getString(R.string.file_not_found), Toast.LENGTH_SHORT).show();
			return;
		}

		String[] parts = FileHelper.getPathAndFilenameOfPdf(pdfFilename);
		String path = parts[0];
		filename = parts[1];

		epubFilename = getEpubFilename(path, filename, saveOnDownloadDirectory);

		tempPath = getExternalCacheDir() + "/";
		oldFilename = tempPath + filename + OLD_EXT;
		tempFilename = tempPath + filename + TEMP_EXT;

		startConversion();
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
        conversionStarted = conversionInProgress();
        finish();
    }

	/**
	 * Save ePUB in the Download folder as user choice or if PDF folder is not writable
	 * @param path
	 * @param filename
	 * @param saveOnDownloadDirectory
	 * @return
	 */
	private String getEpubFilename(String path, String filename, boolean saveOnDownloadDirectory){
		boolean writable = FileHelper.folderIsWritable(path);
		String outputDirectory = (saveOnDownloadDirectory || !writable) ? getDownloadDirectory(): path;
		return outputDirectory + filename + EPUB_EXT;
	}

	private String getDownloadDirectory(){
		return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/";
	}

	private void setupButtons() {
		bt_ok = (Button)findViewById(R.id.ok);
		bt_stop_conversion = (Button)findViewById(R.id.stop);

		bt_ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				conversionStarted = false;
				finish();
			}
		});
		bt_stop_conversion.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				result = ConversionStatus.CONVERSION_STOPPED_BY_USER;
				convertTask.cancel(true);
				sendFinishNotification();
			}
		});
	}

	private void getPrefs() {
		PreferencesHelper prefs = new PreferencesHelper(this);

		includeImages = prefs.getBoolean(PreferencesKeys.ADD_EXTRACTED_IMAGES_FROM_PDF, true);
		repeatedImages = prefs.getBoolean(PreferencesKeys.ADD_REPEATED_IMAGES);
		pagesPerFile = prefs.getParsedString(PreferencesKeys.PAGES_PER_FILE, 5);
		onError = prefs.getParsedString(PreferencesKeys.OPTION_WHEN_ERROR_IN_CONVERSION, DecissionOnConversionError.KEEP_ITEM);
		addMarkers = prefs.getBoolean(PreferencesKeys.MARK_ERRORS, true);
		tocFromPdf = prefs.getBoolean(PreferencesKeys.TRY_TO_EXTRACT_TOC_FROM_PDF, true);
		showLogoOnCover = prefs.getBoolean(PreferencesKeys.HAVE_LOGO_ON_COVER, true);
		saveOnDownloadDirectory = prefs.getBoolean(PreferencesKeys.SAVE_ALWAYS_ON_DOWNLOAD_DIRECTORY);
	}

	private void setButtons(boolean flag) {
		okBtEnabled = flag;
		bt_ok.setEnabled(okBtEnabled);
		bt_stop_conversion.setEnabled(!okBtEnabled);
	}

	// Conversion in progress?
	public boolean conversionInProgress() {
		return !okBtEnabled;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if(id!=0){
			return null;
		}

		return new AlertDialog.Builder(ConvertActivity.this)
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
					deleteTmp();
				}
			})
			.setNeutralButton(getResources().getString(R.string.verify_epub), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					Intent verify = new Intent(getBaseContext(), VerifyActivity.class);
					verify.putExtra(BundleKeys.FILENAME, tempFilename);
					startActivityForResult(verify, 0);
				}
			})
			.create();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		showDialog(0);
	}

	private void deleteTmp() {
		new File(tempFilename).delete();
		if (new File(oldFilename).exists()) {
			new File(oldFilename).renameTo(new File(epubFilename));
			updateProgressText(getResources().getString(R.string.kept_old_epub));
		} else {
			updateProgressText(getResources().getString(R.string.epub_was_deleted));
		}
	}

	private void keepEpub() {
		updateProgressText("\n" + getResources().getStringArray(R.array.conversion_result_message)[0] + "\n");
		if (addMarkers) {
			String pageNumberString = getResources().getString(R.string.pagenumber, ">>\n");
			updateProgressText(getResources().getString(R.string.errors_are_marked_with, "<<@") + pageNumberString);
			updateProgressText(getResources().getString(R.string.lost_pages_are_marked_with, "<<#") + pageNumberString);
		}
		renameFile();
		updateProgressText(getResources().getString(R.string.epubfile, epubFilename));
	}

	private void renameFile() {
		new File(tempFilename).renameTo(new File(epubFilename));
		new File(oldFilename).delete();
	}

    public void startConversion(){
		sendStartNotification();
		result = ConversionStatus.SUCCESS;

		setButtons(false);
		conversionStarted = true;

		convertTask = new ConvertTask();
		convertTask.execute();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onConversionFinished(ConversionFinishedEvent event){
		result = event.getResult();
		sendFinishNotification();
		setButtons(true);
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
			.setContentText(filename)
			.setStyle(new NotificationCompat.BigTextStyle().bigText(filename))
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

		private StringBuilder progressSb;

		public ConvertTask() {
			progressSb = new StringBuilder();
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
			return loadPDF();
		}

		@Override
		protected void onPostExecute(Integer result) {
			if (result == ConversionStatus.EXTRACTION_ERROR) {
				if (onError == DecissionOnConversionError.KEEP_ITEM) {
					// Keep ePUB with errors
					keepEpub();
					result = ConversionStatus.SUCCESS;
				} else if (onError == DecissionOnConversionError.DISCARD_ITEM){
					// Drop ePUB with errors
					deleteTmp();
				} else {
					// Ask for keeping ePUB with errors
					if (hasWindowFocus()) {
						showDialog(0);
					} else {
						keepEpub();
						result = ConversionStatus.SUCCESS;
					}
				}
			} else {
				// Delete on failure
				publishProgress("\n" + getResources().getStringArray(R.array.conversion_result_message)[result]);
				if(result == ConversionStatus.SUCCESS){
					//Keep if ok
					renameFile();
					publishProgress(getResources().getString(R.string.epubfile, epubFilename));
				} else {
					deleteTmp();
				}
			}

			EventBus.getDefault().post(new ConversionFinishedEvent(result));
		}

		private int loadPDF() {
			publishProgress(getResources().getString(R.string.load, pdfFilename));

			if (!(new File(pdfFilename).exists())) {
				return ConversionStatus.FILE_NOT_FOUND;
			}

			if (PdfReadHelper.open(pdfFilename)) {
				return ConversionStatus.CANNOT_READ_PDF;
			}

			return fillEpub();
		}

		private void saveOldEPUB() {
			if (new File(epubFilename).exists()) {
				new File(epubFilename).renameTo(new File(oldFilename));
			}
		}

		private void removeCacheFiles() {
			FileHelper.deleteFilesFromDirectory(new File(tempPath));
		}

		private int fillEpub() {
			try {
				int pages = PdfReadHelper.getPages();
				publishProgress(getResources().getString(R.string.number_of_pages, pages));
				int totalFiles = 2 + pages;
				int writedFiles = 0;

				publishProgress(getResources().getString(R.string.create_epub));
				if (ZipWriter.create(tempFilename)) {
					return ConversionStatus.CANNOT_WRITE_EPUB;
				}

				publishProgress(getResources().getString(R.string.mimetype));
				if (presenter.addMimeType()) {
					return ConversionStatus.CANNOT_WRITE_EPUB;
				}

				publishProgress(getResources().getString(R.string.container));
				if (presenter.addContainer()) {
					return ConversionStatus.CANNOT_WRITE_EPUB;
				}

				String title = filename.replaceAll("[^\\p{Alnum}]", " ");
				String bookId = title + " - " + new Date().hashCode();

				publishProgress(getResources().getString(R.string.toc));
				if (presenter.addToc(pages, bookId, title, tocFromPdf, pagesPerFile)) {
					return ConversionStatus.CANNOT_WRITE_EPUB;
				}

				publishProgress(getResources().getString(R.string.frontpage));
				if (presenter.addFrontPage()) {
					return ConversionStatus.CANNOT_WRITE_EPUB;
				}

				publishProgress(getResources().getString(R.string.frontpagepng));
				if (presenter.addFrontpageCover(filename, cover_file, showLogoOnCover)) {
					return ConversionStatus.CANNOT_WRITE_EPUB;
				}

				boolean extractionErrorFlag = false;

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
							extractionErrorFlag = true;
							if (addMarkers) {
								pageText.append("&lt;&lt;#" + j + "&gt;&gt;");
							}
						} else {
							if (page.matches(".*\\p{Cntrl}.*")) {
								extractionErrorFlag = true;
								if (addMarkers) {
									pageText.append(page.replaceAll("\\p{Cntrl}+", "&lt;&lt;@" + j + "&gt;&gt;"));
								} else {
									pageText.append(page.replaceAll("\\p{Cntrl}+", " "));
								}
							} else {
								pageText.append(page);
							}
						}

						if (includeImages) {
							List<String> imageList = PdfReadHelper.getPageImages(j);
							for (String imageName: imageList){
								String imageTag = "\n<img alt=\"" + imageName + "\" src=\"" + imageName + "\" /><br/>";

								if (!allImageList.contains(imageName)) {
									allImageList.add(imageName);
									publishProgress(getResources().getString(R.string.image_added, imageName));
									pageText.append(imageTag);
								} else if (repeatedImages) {
									pageText.append(imageTag);
								}
							}
						}
						// Close page
						pageText.append("\n  </p>\n");
					}

					if (presenter.addPage(i, pageText.toString())) {
						return ConversionStatus.CANNOT_WRITE_EPUB;
					}
				}

				publishProgress(getResources().getString(R.string.content));
				if (presenter.addContent(pages, bookId, allImageList, title, pagesPerFile)) {
					return ConversionStatus.CANNOT_WRITE_EPUB;
				}

				publishProgress(getResources().getString(R.string.close_file));
				if (ZipWriter.close()) {
					return ConversionStatus.CANNOT_WRITE_EPUB;
				}

				if (extractionErrorFlag) {
					return ConversionStatus.EXTRACTION_ERROR;
				} else {
					return ConversionStatus.SUCCESS;
				}
			} catch(OutOfMemoryError e) {
				return ConversionStatus.OUT_OF_MEMORY_ERROR;
			}
		}
	}
}
