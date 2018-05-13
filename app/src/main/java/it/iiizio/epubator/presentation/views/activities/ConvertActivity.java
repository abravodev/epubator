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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import it.iiizio.epubator.domain.entities.ConversionSettings;
import it.iiizio.epubator.domain.services.ConversionServiceImpl;
import it.iiizio.epubator.infrastructure.providers.SharedPreferenceProviderImpl;
import it.iiizio.epubator.infrastructure.providers.StorageProvider;
import it.iiizio.epubator.infrastructure.providers.StorageProviderImpl;
import it.iiizio.epubator.infrastructure.services.ConversionService;
import it.iiizio.epubator.presentation.events.ConversionCanceledEvent;
import it.iiizio.epubator.presentation.events.ConversionFinishedEvent;
import it.iiizio.epubator.presentation.events.ConversionStatusChangedEvent;
import it.iiizio.epubator.presentation.events.ProgressUpdateEvent;
import it.iiizio.epubator.presentation.presenters.ConvertPresenter;
import it.iiizio.epubator.presentation.presenters.ConvertPresenterImpl;
import it.iiizio.epubator.presentation.utils.ContextHelper;

public class ConvertActivity extends AppCompatActivity {

	//<editor-fold desc="Attributes">
	private static TextView tv_conversionStatus;
	private static ScrollView sv_progress;
	private static TextView tv_progress;
	private static Button bt_ok;
	private static Button bt_stopConversion;

	private static int result;

	private static ConversionSettings settings;
	private ConvertPresenter presenter;
	//</editor-fold>

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_conversion);

		presenter = makePresenter();

		setupElements();

		boolean startConversion = getIntent().getBooleanExtra(BundleKeys.START_CONVERSION, false);
		if(startConversion){
			updateResult(ConversionStatus.NOT_STARTED);
			getIntent().removeExtra(BundleKeys.START_CONVERSION);
		}

		if (conversionInProgress() || conversionFinished()) {
			updateButtonStates();
			return;
		}

		String pdfFilename = getIntent().getStringExtra(BundleKeys.FILENAME);
		if(pdfFilename==null){
			Toast.makeText(this, getString(R.string.file_not_found), Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		String coverFile = getIntent().getStringExtra(BundleKeys.COVER);
		settings = presenter.getConversionSettings(pdfFilename, coverFile);
		startConversion(settings);
		updateButtonStates();
	}

	private ConvertPresenter makePresenter() {
		StorageProvider storageProvider = new StorageProviderImpl(this);
		return new ConvertPresenterImpl(new SharedPreferenceProviderImpl(this),
			new ConversionServiceImpl(storageProvider), new StorageProviderImpl(this));
	}

	@Override
    public void onResume() {
        super.onResume();
		EventBus.getDefault().register(this);
		setupConversionSummary(settings);
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

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		String conversionFinishedText = intent.getStringExtra(BundleKeys.CONVERSION_TEXT);
		if(conversionFinishedText!=null){
			updateProgressText(conversionFinishedText);
			updateButtonStates(false);
		}
	}

	@Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
	public void onProgressUpdate(ProgressUpdateEvent event){
		updateProgressText(event.getMessage());
	}

	@Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
	public void onConversionStatusChanged(ConversionStatusChangedEvent event){
		updateResult(event.getConversionStatus());
		updateConversionStatus(result);
		updateButtonStates();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onConversionFinished(ConversionFinishedEvent event){
		updateResult(event.getResult());
		updateButtonStates();
		if(event.actionRequested()){
			handleConvertedFileAfterError(event.getSettings());
		}
	}

	//<editor-fold desc="Private methods">
	private void updateConversionStatus(int status){
		String textStatus = getConversionStatus(status);
		tv_conversionStatus.setText(textStatus);
	}

	/**
	 * Ask user what to do with the converted file after there has been any error
	 */
	private void handleConvertedFileAfterError(final ConversionSettings settings){
		new AlertDialog.Builder(ConvertActivity.this)
			.setTitle(getResources().getString(R.string.extraction_error))
			.setMessage(getResources().getString(R.string.keep_epub_file))
			.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					keepEpub(settings);
					// TODO: Update the notification (remove action requested)
				}
			})
			.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					deleteTemporalFile(settings);
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

	private void startConversion(ConversionSettings settings){
		if(ContextHelper.isServiceRunning(this, ConversionService.class)) {
			return; // Service already started
		}

		EventBus.getDefault().postSticky(new ProgressUpdateEvent());
		updateResult(ConversionStatus.IN_PROGRESS);
		updateButtonStates();
		Intent conversionServiceIntent = new Intent(this, ConversionService.class);
		conversionServiceIntent.putExtra(BundleKeys.CONVERSION_SETTINGS, settings);
		startService(conversionServiceIntent);
	}

	private void setupConversionSummary(ConversionSettings settings){
		TextView tv_sourceFilename = (TextView) findViewById(R.id.tv_source_filename);
		tv_sourceFilename.setText(settings.pdfFilename);

		TextView tv_epubFilename = (TextView) findViewById(R.id.tv_epub_filename);
		tv_epubFilename.setText(settings.epubFilename);

		tv_conversionStatus = (TextView) findViewById(R.id.tv_conversion_status);
		updateConversionStatus(result);
	}

	private void setupElements() {
		sv_progress = (ScrollView) findViewById(R.id.sv_conversion_view);
		tv_progress = (TextView) findViewById(R.id.tv_conversion_progress);

		bt_ok = (Button) findViewById(R.id.bt_ok);
		bt_stopConversion = (Button) findViewById(R.id.bt_stop_conversion);

		bt_ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		bt_stopConversion.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				updateResult(ConversionStatus.CONVERSION_STOPPED_BY_USER);
				EventBus.getDefault().post(new ConversionCanceledEvent(result));
				updateButtonStates();
			}
		});
	}

	private void updateButtonStates() {
		updateButtonStates(conversionInProgress());
	}

	private void updateButtonStates(boolean conversionInProgress) {
		bt_ok.setEnabled(!conversionInProgress); // Ok button only enabled before or after conversion
		bt_stopConversion.setEnabled(conversionInProgress); // Stop button only enabled during conversion
	}

	private void deleteTemporalFile(ConversionSettings settings) {
		boolean exists = presenter.deleteTemporalFile(settings);
		if (exists) {
			updateProgressText(getResources().getString(R.string.kept_old_epub));
		} else {
			updateProgressText(getResources().getString(R.string.epub_was_deleted));
		}
	}

	private void keepEpub(ConversionSettings settings) {
		updateProgressText("\n" + getConversionStatus(ConversionStatus.SUCCESS) + "\n");
		if (settings.getPreferences().addMarkers) {
			String pageNumberString = getResources().getString(R.string.pagenumber, ">>\n");
			updateProgressText(getResources().getString(R.string.errors_are_marked_with, "<<@") + pageNumberString);
			updateProgressText(getResources().getString(R.string.lost_pages_are_marked_with, "<<#") + pageNumberString);
		}
		presenter.saveEpub(settings);
		updateProgressText(getResources().getString(R.string.epubfile, settings.epubFilename));
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

	private boolean conversionInProgress(){
		return result == ConversionStatus.IN_PROGRESS
			|| result == ConversionStatus.LOADING_FILE;
	}

	private boolean conversionFinished(){
		return result == ConversionStatus.SUCCESS;
	}

	private void updateResult(int result){
		ConvertActivity.result = result;
	}

	private String getConversionStatus(int conversionStatus){
		return getResources().getStringArray(R.array.conversion_result_message)[conversionStatus];
	}
	//</editor-fold>
}