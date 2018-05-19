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
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import java.net.URISyntaxException;

import it.iiizio.epubator.R;
import it.iiizio.epubator.domain.constants.BundleKeys;
import it.iiizio.epubator.domain.constants.FileTypes;
import it.iiizio.epubator.infrastructure.providers.SharedPreferenceProviderImpl;
import it.iiizio.epubator.infrastructure.providers.StorageProviderImpl;
import it.iiizio.epubator.infrastructure.providers.ViewPreferenceProviderImpl;
import it.iiizio.epubator.presentation.presenters.MainPresenter;
import it.iiizio.epubator.presentation.presenters.MainPresenterImpl;
import it.iiizio.epubator.presentation.utils.IntentHelper;
import it.iiizio.epubator.presentation.utils.PathUtils;
import it.iiizio.epubator.presentation.utils.PermissionHelper;

public class MainActivity extends AppCompatActivity {

	//<editor-fold desc="Attributes">
	private static final int REQUEST_PERMISSION_CODE = 23;
	private String filename = "";
	private MainPresenter presenter;
	//</editor-fold>

	//<editor-fold desc="Inner class">
	private static class Actions {
		static final int CONVERT = 1;
		static final int VERIFY = 2;
		static final int PICK_A_PIC = 3;
	}
	//</editor-fold>

	//<editor-fold desc="Methods">
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		presenter = buildPresenter();

		PermissionHelper.checkWritePermission(this, REQUEST_PERMISSION_CODE); // TODO: Only check before requesting it
		setupButtons();
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
			case R.id.prefs: gotoView(PreferencesActivity.class); return true;
			case R.id.quickstart: showQuickStartDialog(); return true;
			case R.id.info: gotoView(InfoActivity.class); return true;
			case R.id.license: gotoView(LicenseActivity.class); return true;
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
				case Actions.PICK_A_PIC: getImageFromGallery(getActualPath(result)); break;
				default: errorWhenChoosingFile(); break;
			}
		} else if (resultCode == RESULT_CANCELED){
			if(requestCode == Actions.PICK_A_PIC) {
				Toast.makeText(this, R.string.image_must_be_picked_from_gallery, Toast.LENGTH_LONG).show();
			}
		}
	}

	private MainPresenter buildPresenter(){
		return new MainPresenterImpl(new ViewPreferenceProviderImpl(this),
			new SharedPreferenceProviderImpl(this),
			new StorageProviderImpl(this));
	}

	// Show quick start on first time
	private void showInitialDialog(){
		if (presenter.showInitialDialog()) {
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

	private void showQuickStartDialog(){
		new AlertDialog.Builder(this)
			.setTitle(getResources().getString(R.string.quickstart))
			.setMessage(getResources().getString(R.string.quickstart_text))
			.setNeutralButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					presenter.initialDialogRead();
				}
			})
			.create()
			.show();
	}

	private void selectFileFromSystem(String fileType, int action){
		try {
			String directory = presenter.getRecentFolder();
			String viewTitle = getString(R.string.select_a_file);
			Intent intent = IntentHelper.openDocument(directory, fileType, viewTitle);
			startActivityForResult(intent, action);
		} catch (ActivityNotFoundException e) {
			// TODO: Handle rejection
		}
	}

	private void selectPdfFileFromSystem(){
		selectFileFromSystem(FileTypes.PDF, Actions.CONVERT);
	}

	private void selectEpubFileFromSystem(int action){
		selectFileFromSystem(FileTypes.EPUB, action);
	}

	private void selectImageFileFromSystem(){
		selectFileFromSystem(FileTypes.ANY_IMAGE, Actions.PICK_A_PIC);
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

	private void gotoConversionView(String selectedCoverFile){
		gotoConversionView(filename, selectedCoverFile);
	}

	private void gotoConversionView(String filename, String coverFile) {
		Intent convert = new Intent(this, ConvertActivity.class);
		convert.putExtra(BundleKeys.FILENAME, filename);
		convert.putExtra(BundleKeys.COVER, coverFile);
		convert.putExtra(BundleKeys.START_CONVERSION, true);
		startActivity(convert);
	}

	private void gotoView(Class viewClass){
		startActivity(new Intent(this, viewClass));
	}

	private void gotoStore() {
		startActivity(IntentHelper.openMarket("iiizio"));
	}

	private void gotoVerifyView(String chosenFile) {
		Intent verify = new Intent(this, VerifyActivity.class);
		verify.putExtra(BundleKeys.FILENAME, chosenFile);
		startActivity(verify);
	}

	private void getImageFromGallery(String selectedImage) {
		gotoConversionView(selectedImage);
	}

	private void convertFile(String chosenFile){
		filename = chosenFile;
		presenter.updateRecentFolder(chosenFile);
		setCoverImage(chosenFile);
	}

	private void setCoverImage(String filename) {
		if (presenter.userPrefersToChoosePicture()) {
            selectImageFileFromSystem();
            return;
        }

		String coverFile = presenter.getCoverFileWithTheSameName(filename);
        gotoConversionView(filename, coverFile);
	}

	private void verifyFile(String chosenFile){
		presenter.updateRecentFolder(chosenFile);
		gotoVerifyView(chosenFile);
	}
	//</editor-fold>
}