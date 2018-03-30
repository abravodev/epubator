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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.view.MenuItemCompat;
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
	private final int CONVERT = 1;
	private final int VERIFY = 2;
	private final int PICKAPIC = 3;
	private final int OPENWITH = 4;
	private final int SHAREWITH = 5;

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
		
		// Show quick start on first time
		if (sharedPref.getBoolean("first_time", true)) {
			showDialog(0);
		}
		
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
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		MenuItemCompat.setShowAsAction(menu.findItem(R.id.prefs), 0);
		return true;
	}

	// Menu item selected
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.prefs:
			startActivity(new Intent(ePUBator.this, Prefs.class));
			return true;
		case R.id.open:
			Intent chooseFile = new Intent(ePUBator.this, FileChooser.class);
			chooseFile.putExtra("path", path);
			chooseFile.putExtra("filter", EPUB_EXT);
			startActivityForResult(chooseFile, OPENWITH);
			return true;
		case R.id.share:
			chooseFile = new Intent(ePUBator.this, FileChooser.class);
			chooseFile.putExtra("path", path);
			chooseFile.putExtra("filter", EPUB_EXT);
			startActivityForResult(chooseFile, SHAREWITH);
			return true;
		case R.id.quickstart:
			showDialog(0);
			return true;
		case R.id.info:
			startActivity(new Intent(ePUBator.this, Info.class));
			return true;
		case R.id.license:
			startActivity(new Intent(ePUBator.this, License.class));
			return true;
		case R.id.my_apps:
			startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("market://search?q=pub:iiizio")));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// Quick start dialog
	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == 0) {
			// Build dialog
			return new AlertDialog.Builder(ePUBator.this)
			.setTitle(getResources().getString(R.string.quickstart))
			.setMessage(getResources().getString(R.string.quickstart_text))
			// Preview action
			.setNeutralButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					SharedPreferences.Editor editor = sharedPref.edit();
					editor.putBoolean("first_time", false);
					editor.commit();
				}
			})
			.create();
		} else
			return null;
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
				startActivityForResult(chooseFile, CONVERT);
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
			startActivityForResult(chooseFile, VERIFY);
		}
	};

	// File selected
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case OPENWITH:
			// Open ePUB
			if (resultCode == RESULT_OK) {
				filename = data.getAction();
				updateRecentFolder();
				try
				{
					Intent sendIntent = new Intent(Intent.ACTION_VIEW);  
					sendIntent.setDataAndType(Uri.fromFile(new File(filename)), "application/epub+zip");
					startActivity(Intent.createChooser(sendIntent, getResources().getString(R.string.open)));
				}
				catch(Exception e)
				{
					System.err.println("Exception in Open with " + e.getMessage());
				}
			}
			break;
		case SHAREWITH:
			// Share ePUB
			if (resultCode == RESULT_OK) {
				filename = data.getAction();
				updateRecentFolder();
				try
				{
					Intent sendIntent = new Intent(Intent.ACTION_SEND);  
					sendIntent.setType("application/epub+zip");
					sendIntent.putExtra(android.content.Intent.EXTRA_STREAM, Uri.fromFile(new File(filename)));
					startActivity(Intent.createChooser(sendIntent, getResources().getString(R.string.share)));
				}
				catch(Exception e)
				{
					System.err.println("Exception in Share with " + e.getMessage());
				}
			}
			break;
		case PICKAPIC:
			// Get image from gallery
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
		default:
			// Conversion or verify
			if (resultCode == RESULT_OK) {
				filename = data.getAction();
				pickActivity();
			}
			break;
		}
	}

	// Start conversion or verify
	protected void pickActivity() {
		updateRecentFolder();

		if (filename.endsWith(PDF_EXT)) {
			if (!cover_picked && !Convert.conversionStarted) {
			SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
				if (prefs.getBoolean("pickapic", false)) {
					// Choose an image
					Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
					photoPickerIntent.setType("image/*");
					startActivityForResult(photoPickerIntent, PICKAPIC);
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
	
	// Update recent folder
	protected void updateRecentFolder() {
	path = filename.substring(0, filename.lastIndexOf('/', filename.length()) + 1);
	SharedPreferences.Editor editor = sharedPref.edit();
	editor.putString("path", path);
	editor.commit();
	}
}