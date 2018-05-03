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
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import java.io.File;
import java.net.URISyntaxException;

import it.iiizio.epubator.R;
import it.iiizio.epubator.domain.constants.BundleKeys;
import it.iiizio.epubator.domain.constants.PreferencesKeys;
import it.iiizio.epubator.presentation.presenters.MainPresenter;
import it.iiizio.epubator.presentation.presenters.MainPresenterImpl;
import it.iiizio.epubator.presentation.utils.PathUtils;
import it.iiizio.epubator.presentation.utils.PermissionHelper;
import it.iiizio.epubator.presentation.utils.PreferencesHelper;

public class MainActivity extends AppCompatActivity {

	private static final int REQUEST_PERMISSION_CODE = 23;
	private String filename = "";
	private String coverFile = "";
	private PreferencesHelper viewPreferencesHelper;
	private PreferencesHelper sharedPreferencesHelper;
	private MainPresenter presenter;

	private static class Actions {
		static final int CONVERT = 1;
		static final int VERIFY = 2;
		static final int PICKAPIC = 3;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		presenter = new MainPresenterImpl();

		PermissionHelper.checkWritePermission(this, REQUEST_PERMISSION_CODE); // TODO: Only check before requesting it
		setupButtons();
		viewPreferencesHelper = PreferencesHelper.getViewPreferences(this);
		sharedPreferencesHelper = PreferencesHelper.getAppPreferences(this);
		showInitialDialog();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.mainmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.prefs: gotoPreferences(); return true;
			case R.id.quickstart: showQuickStartDialog(); return true;
			case R.id.info: gotoInfoView(); return true;
			case R.id.license: gotoLicenseView(); return true;
			case R.id.my_apps: gotoStore(); return true;
			default: return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent result) {
		if (resultCode == RESULT_OK){
			switch (requestCode) {
				case Actions.CONVERT: convertFile(getActualPath(result)); break;
				case Actions.VERIFY: verifyFile(getActualPath(result)); break;
				case Actions.PICKAPIC: getImageFromGallery(getActualPath(result)); break;
				default: errorWhenChoosingFile(); break;
			}
		}
	}

	// Show quick start on first time
	private void showInitialDialog(){
		if (viewPreferencesHelper.getBoolean(PreferencesKeys.FIRST_TIME_APP, true)) {
			showQuickStartDialog();
		}
	}

	private void setupButtons() {
		findViewById(R.id.bt_convert_pdf).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				selectPdfFileFromSystem();
				// TODO: Control when there are files in the queue for converting pdfs
			}
		});
		findViewById(R.id.bt_verify_epub).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				selectEpubFileFromSystem(Actions.VERIFY);
			}
		});
	}

	private void gotoPreferences() {
		startActivity(new Intent(MainActivity.this, PreferencesActivity.class));
	}

	private void gotoStore() {
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:iiizio")));
	}

	private void gotoLicenseView() {
		startActivity(new Intent(MainActivity.this, LicenseActivity.class));
	}

	private void gotoInfoView() {
		startActivity(new Intent(MainActivity.this, InfoActivity.class));
	}

	private void gotoVerifyView(String chosenFile) {
		Intent verify = new Intent(MainActivity.this, VerifyActivity.class);
		verify.putExtra(BundleKeys.FILENAME, chosenFile);
		startActivity(verify);
	}

	private void gotoConversionView() {
		Intent convert = new Intent(MainActivity.this, ConvertActivity.class);
		convert.putExtra(BundleKeys.FILENAME, filename);
		convert.putExtra(BundleKeys.COVER, coverFile);
		convert.putExtra(BundleKeys.START_CONVERSION, true);
		startActivity(convert);
	}

	private void showQuickStartDialog(){
		new AlertDialog.Builder(MainActivity.this)
			.setTitle(getResources().getString(R.string.quickstart))
			.setMessage(getResources().getString(R.string.quickstart_text))
			.setNeutralButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					viewPreferencesHelper.save(PreferencesKeys.FIRST_TIME_APP, false);
				}
			})
			.create()
			.show();
	}

	private void selectFileFromSystem(String filetype, int action){
		File file = new File(getRecentFolder());
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intent.setDataAndType(Uri.fromFile(file), filetype);

		try {
			startActivityForResult(intent.createChooser(intent, "Select file"), action);
		} catch (ActivityNotFoundException e) {
			// TODO: Handle rejection
		}
	}

	private void selectPdfFileFromSystem(){
		selectFileFromSystem("application/pdf", Actions.CONVERT);
	}

	private void selectEpubFileFromSystem(int action){
		selectFileFromSystem("application/epub+zip", action);
	}

	private void selectImageFileFromSystem(){
		selectFileFromSystem("image/*", Actions.PICKAPIC);
	}

	private String getActualPath(Intent result){
		try {
			return PathUtils.getPath(this, result.getData());
		} catch (URISyntaxException e) {
			return "";
		}
	}

	private void errorWhenChoosingFile(){
		Toast.makeText(this, R.string.cannot_choose_file, Toast.LENGTH_SHORT).show();
	}

	private void getImageFromGallery(String selectedImage) {
		coverFile = selectedImage;
		gotoConversionView();
	}

	private void convertFile(String chosenFile){
		filename = chosenFile;
		updateRecentFolder(chosenFile);
		setCoverImage();
	}

	private void setCoverImage() {
		boolean userPrefersToUsePicture = sharedPreferencesHelper.getBoolean(PreferencesKeys.CHOOSE_PICTURE);
		if (userPrefersToUsePicture) {
            coverFile = "";
            selectImageFileFromSystem();
            return;
        }

		coverFile = presenter.getCoverFileWithTheSameName(filename);
        gotoConversionView();
	}

	private void verifyFile(String chosenFile){
		updateRecentFolder(chosenFile);
		gotoVerifyView(chosenFile);
	}

	private String getRecentFolder(){
		return sharedPreferencesHelper.getString(PreferencesKeys.PATH, Environment.getExternalStorageDirectory().getPath());
	}

	private void updateRecentFolder(String filename) {
		String path = filename.substring(0, filename.lastIndexOf('/', filename.length()) + 1);
		viewPreferencesHelper.save(PreferencesKeys.PATH, path);
	}
}