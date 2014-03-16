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

package it.iiizio.epubator;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.view.MenuCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class ePUBator extends Activity {
	String filename = "";
	String cover_file = "";
	static String path;
	static boolean cover_picked = false;
	private SharedPreferences sharedPref;
	private final String PDF_EXT = ".pdf";
	private final String EPUB_EXT = " - ePUBator.epub";
	private final int SELECT_PDF = 1;
	private final int SELECT_EPUB = 2;
	private final int SELECT_IMAGE = 3;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		((Button) findViewById(R.id.convert)).setOnClickListener(mConvertListener);
		((Button) findViewById(R.id.verify)).setOnClickListener(mVerifyListener);
		
	    Intent intent = getIntent();
		
		// Get last path
		sharedPref = this.getPreferences(Context.MODE_PRIVATE);
		path = sharedPref.getString("path", Environment.getExternalStorageDirectory().getPath());
		
	    // To get the action of the intent use
	    String action = intent.getAction();
	    if (action.equals(Intent.ACTION_VIEW)) {
			filename = intent.getDataString().replaceAll("%20", " ");
			if (filename.startsWith("file://")) {
				filename = filename.replace("file://", "");
				pickActivity();
			}
	    }
	}

	// Inflate menu
	@SuppressWarnings("deprecation")
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		MenuCompat.setShowAsAction(menu.findItem(R.id.prefs), 1);
		return true;
	}

	// Menu item selected
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.prefs:
			startActivity(new Intent(ePUBator.this, Prefs.class));
			return true;
		case R.id.info:
			startActivity(new Intent(ePUBator.this, Info.class));
			return true;
		case R.id.license:
			startActivity(new Intent(ePUBator.this, License.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// Convert button pressed
	View.OnClickListener mConvertListener = new OnClickListener() {
		public void onClick(View v) {
			if (Convert.started()) {
				// Conversion already started, show progress
				if (Convert.working()) {
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.cip), Toast.LENGTH_SHORT).show();
				}
				startActivity(new Intent(ePUBator.this, Convert.class));
			} else {
				// Select a file
				Intent chooseFile = new Intent(ePUBator.this, FileChooser.class);
				chooseFile.putExtra("path", path);
				chooseFile.putExtra("filter", PDF_EXT);
				startActivityForResult(chooseFile, SELECT_PDF);
			}
		}
	};

	// Verify button pressed
	View.OnClickListener mVerifyListener = new OnClickListener() {
		public void onClick(View v) {
			// Select a file
			Intent chooseFile = new Intent(ePUBator.this, FileChooser.class);
			chooseFile.putExtra("path", path);
			chooseFile.putExtra("filter", EPUB_EXT);
			startActivityForResult(chooseFile, SELECT_EPUB);
		}
	};

	// File selected
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
		case SELECT_PDF:
		case SELECT_EPUB:
			if (resultCode == RESULT_OK) {
				filename = data.getAction();
				pickActivity();
			}
			break;
		case SELECT_IMAGE:
			if (resultCode == RESULT_OK) {
				Uri selectedImage = data.getData();
				String[] filePathColumn = {MediaStore.Images.Media.DATA};

	            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
	            cursor.moveToFirst();

	            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
	            cover_file = cursor.getString(columnIndex);
	            cursor.close();
			} else {
				cover_file = "";
			}
			cover_picked = true;
			pickActivity();
			break;
		}
	}

	// Start conversion or verify
	protected void pickActivity() {
		path = filename.substring(0, filename.lastIndexOf('/', filename.length()) + 1);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString("path", path);
		editor.commit();

		if (filename.endsWith(PDF_EXT)) {
			boolean pickapic = true; // TODO
			if (!cover_picked && !Convert.conversionStarted) {
				if (pickapic) {
					// Choose an image
					Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
					photoPickerIntent.setType("image/*");
					startActivityForResult(photoPickerIntent, SELECT_IMAGE);
				} else {
					// Check if there an image with the same name of PDF file
					String name = filename.substring(0, filename.lastIndexOf(PDF_EXT));
					cover_file = "";

					if(new File(name + ".png").exists()) {
						cover_file = name + ".png";
					} else if(new File(name + ".jpg").exists()) {
						cover_file = name + ".jpg";
					} else if(new File(name + ".jpeg").exists()) {
						cover_file = name + ".jpeg";
					}
					cover_picked = true;
				}
			}

			if(cover_picked) {
				// Start conversion
				cover_picked = false;
				Intent convert = new Intent(ePUBator.this, Convert.class);
				convert.putExtra("filename", filename);
				convert.putExtra("cover", cover_file);
				startActivity(convert);
			}
		} else if (filename.endsWith(EPUB_EXT)) {
			// Show ePUB file
			Intent verify = new Intent(ePUBator.this, Verify.class);
			verify.putExtra("filename", filename);
			startActivity(verify);
		} else {
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.wrongfile), Toast.LENGTH_SHORT).show();
		}
	}
}