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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

public class Preview extends Activity {
	private static int pageNumber = -1;
	private static ArrayList<String>pageList;
	private static ZipFile epubFile = null;
	private static WebView previewWv;
	private static Button prevBt;
	private static Button nextBt;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preview);
		
		if (pageNumber == -1) {
			previewWv = (WebView)findViewById(R.id.preview);
			prevBt = (Button)findViewById(R.id.prev);
			nextBt = (Button)findViewById(R.id.next);

			fillPageList();
			if (pageList.isEmpty()) {
				closeEpub();
				readError();
			} else {
				pageNumber = 1;
			}
		}
		
		showPage(0);
		prevBt.setOnClickListener(mPrevListener);
		nextBt.setOnClickListener(mNextListener);
	}
	
	// Show page
	private void showPage(int diff) {
		pageNumber += diff;
		
		if (pageNumber == 1) {
			prevBt.setEnabled(false);
		} else {
			prevBt.setEnabled(true);
		}
		if  (pageNumber == pageList.size()) {
			nextBt.setEnabled(false);
		} else {
			nextBt.setEnabled(true);
		}

		String pageName = pageList.get(pageNumber - 1);
		StringBuilder htmlPage = new StringBuilder();
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(epubFile.getInputStream(epubFile.getEntry(pageName))));
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				htmlPage.append(line);
			}
		} catch (IOException e) {
			readError();
		}

		previewWv.loadData(htmlPage.toString(), "text/html", "utf-8");
	}

	// Prev button pressed
	View.OnClickListener mPrevListener = new OnClickListener() {
		public void onClick(View v) {
			showPage(-1);
		}
	};
	
	// Next button pressed
	View.OnClickListener mNextListener = new OnClickListener() {
		public void onClick(View v) {
			showPage(+1);
		}
	};

	// Fill page list
	private void fillPageList() {
		// Get filename
		String filename = "";
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if (extras.containsKey("filename")) {
				filename = extras.getString("filename");
			}
		}
		
		// open ePUB file
		try {
			epubFile = new ZipFile(filename);
		} catch (IOException e) {
			readError();
		}
		
		// Get page list
		pageList = new ArrayList<String>();
		Enumeration<? extends ZipEntry>fileList;
		for (fileList = epubFile.entries(); fileList.hasMoreElements();) {
            ZipEntry entry = (ZipEntry) fileList.nextElement();
            String name = entry.getName();
            if (name.contains("page")) {
            	pageList.add(name);
            }
        }
	}

	// Show error toast
	private void readError() {
		Toast.makeText(getApplicationContext(), getResources().getString(R.string.read_error), Toast.LENGTH_SHORT).show();
		exit();
	}

	// Activity end
	private void exit() {
		pageNumber = -1;
		finish();
	}

	// Back button pressed
	@Override
	public void onBackPressed() {
		closeEpub();
		exit();
	}

	// Close ePUB file
	private void closeEpub() {
		try {
			epubFile.close();
		} catch (IOException e) {
			readError();
		}
	}
}
