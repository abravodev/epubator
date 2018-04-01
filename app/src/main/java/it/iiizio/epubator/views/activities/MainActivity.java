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
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.net.URISyntaxException;

import it.iiizio.epubator.R;
import it.iiizio.epubator.model.constants.BundleKeys;
import it.iiizio.epubator.model.constants.PreferencesKeys;
import it.iiizio.epubator.presenters.MainPresenter;
import it.iiizio.epubator.presenters.MainPresenterImpl;
import it.iiizio.epubator.views.utils.PathUtils;
import it.iiizio.epubator.views.utils.PermissionHelper;

public class MainActivity extends Activity {

	private String filename = "";
	private String cover_file = "";
	private static boolean cover_picked = false;
	private SharedPreferences sharedPref;
	private MainPresenter presenter;

	private static class Actions {
		static final int CONVERT = 1;
		static final int VERIFY = 2;
		static final int PICKAPIC = 3;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		presenter = new MainPresenterImpl();

		PermissionHelper.checkWritePermission(this); // TODO: Only check before requesting it
		setupButtons();

		sharedPref = this.getPreferences(Context.MODE_PRIVATE);

		showInitialDialog();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		MenuItemCompat.setShowAsAction(menu.findItem(R.id.prefs), 0);
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
		if (sharedPref.getBoolean("first_time", true)) {
			showQuickStartDialog();
		}
	}

	private void setupButtons() {
		((Button) findViewById(R.id.convert)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				selectPdfFileFromSystem();
				// TODO: Control when there are files in the queue for converting pdfs
			}
		});
		((Button) findViewById(R.id.verify)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				selectEpubFileFromSystem(Actions.VERIFY);
			}
		});
	}

	private void gotoPreferences() {
		startActivity(new Intent(MainActivity.this, PrefsActivity.class));
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

	private void gotoVerifyView() {
		Intent verify = new Intent(MainActivity.this, VerifyActivity.class);
		verify.putExtra(BundleKeys.FILENAME, filename);
		startActivity(verify);
	}

	private void gotoConversionView() {
		Intent convert = new Intent(MainActivity.this, ConvertActivity.class);
		convert.putExtra(BundleKeys.FILENAME, filename);
		convert.putExtra(BundleKeys.COVER, cover_file);
		startActivity(convert);
	}

	private void showQuickStartDialog(){
		new AlertDialog.Builder(MainActivity.this)
			.setTitle(getResources().getString(R.string.quickstart))
			.setMessage(getResources().getString(R.string.quickstart_text))
			.setNeutralButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					SharedPreferences.Editor editor = sharedPref.edit();
					editor.putBoolean("first_time", false);
					editor.commit();
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
		cover_file = selectedImage;
		cover_picked = true;
		convertFile();
	}

	private void convertFile(String chosenFile){
		filename = chosenFile;

		updateRecentFolder(chosenFile);
		convertFile();
	}

	private void convertFile(){
		if (!cover_picked && !ConvertActivity.conversionStarted) {
			setCoverImage();
		}

		if(cover_picked) {
			cover_picked = false;
			gotoConversionView();
		}
	}

	private void setCoverImage() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean userPrefersToUsePicture = prefs.getBoolean(PreferencesKeys.CHOOSE_PICTURE, false);
		if (userPrefersToUsePicture) {
            cover_file = "";
            selectImageFileFromSystem();
        } else {
            cover_file = presenter.getCoverFileWithTheSameName(filename);

            cover_picked = true;
        }
	}

	private void verifyFile(String chosenFile){
		updateRecentFolder(chosenFile);
		gotoVerifyView();
	}

	private String getRecentFolder(){
		return sharedPref.getString(PreferencesKeys.PATH, Environment.getExternalStorageDirectory().getPath());
	}

	private void updateRecentFolder(String filename) {
		String path = filename.substring(0, filename.lastIndexOf('/', filename.length()) + 1);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(PreferencesKeys.PATH, path);
		editor.commit();
	}
}