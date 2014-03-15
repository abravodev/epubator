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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
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
	static String path;
	private SharedPreferences sharedPref;
	private final String PDF_EXT = ".pdf";
	private final String EPUB_EXT = " - ePUBator.epub";

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
				startActivityForResult(chooseFile, 0);
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
			startActivityForResult(chooseFile, 0);
		}
	};

	// File selected
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			filename = data.getAction();
			pickActivity();		}
	}

	// Start conversion or verify
	protected void pickActivity() {
		path = filename.substring(0, filename.lastIndexOf('/', filename.length()) + 1);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString("path", path);
		editor.commit();

		if (filename.endsWith(PDF_EXT)) {
			Intent convert = new Intent(ePUBator.this, Convert.class);
			convert.putExtra("filename", filename);
			startActivity(convert);
		} else if (filename.endsWith(EPUB_EXT)) {
			Intent verify = new Intent(ePUBator.this, Verify.class);
			verify.putExtra("filename", filename);
			startActivity(verify);
		} else {
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.wrongfile), Toast.LENGTH_SHORT).show();
		}
	}
}