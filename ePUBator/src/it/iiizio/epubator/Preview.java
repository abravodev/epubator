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
	private static ZipFile epubFile = null;
	private ArrayList<String>pageList;
	private WebView previewWv;
	private Button prevBt;
	private Button nextBt;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preview);

		previewWv = (WebView)findViewById(R.id.webview);
		previewWv.setBackgroundColor(0);
		prevBt = (Button)findViewById(R.id.prev);
		nextBt = (Button)findViewById(R.id.next);

		// Initialize
		fillPageList();
		if (pageNumber == -1) {
			pageNumber = 1;
		}

		showPage(0);
		prevBt.setOnClickListener(mPrevListener);
		nextBt.setOnClickListener(mNextListener);
	}

	// Show page
	private void showPage(int diff) {
		// No pages
		if (pageList.size() == 0) {
			closeEpub();
			readError();
			return;
		}

		// Move to prev/next page
		pageNumber += diff;

		// Set buttons
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

		// get html page
		String pageName = pageList.get(pageNumber - 1);
		StringBuilder htmlPageSb = new StringBuilder();
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(epubFile.getInputStream(epubFile.getEntry(pageName))));
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				htmlPageSb.append(line);
			}
		} catch (IOException e) {
			readError();
		}

		// Show page in white on black
		String htmlPage = htmlPageSb.toString().replace("<body>", "<body bgcolor=\"Black\"><font color=\"White\">").replace("</body>", "</font></body>");
		previewWv.loadData(htmlPage, "text/html", "utf-8");
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
			if (name.startsWith("OEBPS/page")) {
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
