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

package it.iiizio.epubator.presentation.views.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import it.iiizio.epubator.R;
import it.iiizio.epubator.domain.constants.BundleKeys;
import it.iiizio.epubator.domain.constants.ConversionStatus;
import it.iiizio.epubator.domain.constants.DecissionOnConversionError;
import it.iiizio.epubator.domain.constants.PreferencesKeys;
import it.iiizio.epubator.infrastructure.services.ConversionService;
import it.iiizio.epubator.presentation.dto.ConversionPreferences;
import it.iiizio.epubator.presentation.dto.ConversionSettings;
import it.iiizio.epubator.presentation.events.ConversionCanceledEvent;
import it.iiizio.epubator.presentation.events.ConversionFinishedEvent;
import it.iiizio.epubator.presentation.events.ProgressUpdateEvent;
import it.iiizio.epubator.presentation.presenters.ConvertPresenter;
import it.iiizio.epubator.presentation.presenters.ConvertPresenterImpl;
import it.iiizio.epubator.presentation.utils.BundleHelper;
import it.iiizio.epubator.presentation.utils.ContextHelper;
import it.iiizio.epubator.presentation.utils.PreferencesHelper;

public class ConvertActivity extends Activity {

	//<editor-fold desc="Attributes">
	private static ScrollView sv_progress;
	private static TextView tv_progress;
	private static Button bt_ok;
	private static Button bt_stopConversion;

	private static boolean conversionInProgress = false;
	private static int result;

	private ConversionPreferences preferences;
	private ConversionSettings settings;
	private ConvertPresenter presenter;
	//</editor-fold>

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_conversion);
		presenter = new ConvertPresenterImpl();
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
			finish();
			return;
		}

		String pdfFilename = BundleHelper.getExtraStringOrDefault(extras, BundleKeys.FILENAME);
		if(pdfFilename==null){
			Toast.makeText(this, getString(R.string.file_not_found), Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		String coverFile = BundleHelper.getExtraStringOrEmpty(extras, BundleKeys.COVER);
		String temporalPath = getExternalCacheDir() + "/";
		settings = new ConversionSettings(preferences, pdfFilename, temporalPath, getDownloadDirectory(), coverFile);
		startConversion(settings);
		setButtons();
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
					// TODO: Update the notification (remove action requested)
				}
			})
			.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					deleteTemporalFile();
					// TODO: Update the notification (remove action requested)
				}
			})
			.setNeutralButton(getResources().getString(R.string.verify_epub), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// TODO: Update the notification (remove action requested)
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

	private void startConversion(ConversionSettings settings){
		if(ContextHelper.isServiceRunning(this, ConversionService.class)) {
			return; // Service already started
		}
		sendStartNotification();
		result = ConversionStatus.SUCCESS;

		conversionInProgress = true;
		setButtons();
		Intent conversionServiceIntent = new Intent(this, ConversionService.class);
		conversionServiceIntent.putExtra(BundleKeys.CONVERSION_PREFERENCES, settings);
		startService(conversionServiceIntent);
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
				EventBus.getDefault().post(new ConversionCanceledEvent());
				sendFinishNotification();
				conversionInProgress = false;
				setButtons();
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
		boolean exists = presenter.deleteTemporalFile(settings);
		if (exists) {
			updateProgressText(getResources().getString(R.string.kept_old_epub));
		} else {
			updateProgressText(getResources().getString(R.string.epub_was_deleted));
		}
	}

	private void keepEpub() {
		updateProgressText("\n" + getResources().getStringArray(R.array.conversion_result_message)[ConversionStatus.SUCCESS] + "\n");
		if (preferences.addMarkers) {
			String pageNumberString = getResources().getString(R.string.pagenumber, ">>\n");
			updateProgressText(getResources().getString(R.string.errors_are_marked_with, "<<@") + pageNumberString);
			updateProgressText(getResources().getString(R.string.lost_pages_are_marked_with, "<<#") + pageNumberString);
		}
		presenter.saveEpub(settings);
		updateProgressText(getResources().getString(R.string.epubfile, settings.epubFilename));
	}

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onProgressUpdate(ProgressUpdateEvent event){
		updateProgressText(event.getMessage());
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onConversionFinished(ConversionFinishedEvent event){
		conversionInProgress = false;
		result = event.getResult();
		setButtons();
		if(event.actionRequested()){
			handleConvertedFileAfterError();
		} else {
			sendFinishNotification();
		}
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
	    Intent openConvertActivityIntent = new Intent(this, ConvertActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                openConvertActivityIntent, 0);

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

}
