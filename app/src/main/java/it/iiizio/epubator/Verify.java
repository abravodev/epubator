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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

public class Verify extends Activity {
	private static ZipFile epubFile = null;
	private ArrayList<String>htmlList;
	private static int htmlIndex = -1;
	private List<String> chapters = new ArrayList<String>();
	private List<String> anchors = new ArrayList<String>();
	private WebView verifyWv;
	private Button prevBt;
	private Button nextBt;
	private String anchor;
	final int BUFFERSIZE = 2048;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		setProgressBarVisibility(true);
		setContentView(R.layout.verify);

		verifyWv = (WebView)findViewById(R.id.webview);
		verifyWv.setBackgroundColor(0);
		verifyWv.getSettings().setUseWideViewPort(true);
		verifyWv.setWebViewClient(new WebViewClient() {
			public void onPageFinished(final WebView view, final String url) {
				// make it jump to the internal link
				if (anchor != null) {
					view.loadUrl(url + "#" + anchor);
					anchor = null;
				}
			}
		});

		prevBt = (Button)findViewById(R.id.prev);
		nextBt = (Button)findViewById(R.id.next);

		// Initialize
		fillPageList();
		if (htmlIndex == -1) {
			htmlIndex = 1;
		}

		changeHtmlFile(0);
		prevBt.setOnClickListener(mPrevListener);
		nextBt.setOnClickListener(mNextListener);
	}

	// Inflate menu
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.verifymenu, menu);
		MenuItemCompat.setShowAsAction(menu.findItem(R.id.index), 1);
		return true;
	}

	// Menu item selected
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.index:
			showDialog(0);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// Select chapter dialog
	@Override
	public Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(Verify.this);
		builder.setTitle(R.string.index)
		.setItems(chapters.toArray(new CharSequence[chapters.size()]), new DialogInterface.OnClickListener() {
			// Move to selected chapter
			public void onClick(DialogInterface dialog, int which) {
				String[] links = anchors.get(which).split("#");
				if (links.length > 1)
					anchor = links[1];
				else
					anchor = null;
				changeHtmlFile(htmlList.indexOf(links[0]) - htmlIndex + 1);
			}
		});
		return builder.create();
	}

	// Change html file
	private void changeHtmlFile(int diff) {
		// No html file
		int htlmFiles = htmlList.size();
		if (htlmFiles == 0) {
			closeEpub();
			readError();
			return;
		}

		// Move to prev/next file
		htmlIndex += diff;

		// Set buttons
		if (htmlIndex <= 1) {
			htmlIndex = 1;
			prevBt.setEnabled(false);
		} else {
			prevBt.setEnabled(true);
		}
		if  (htmlIndex >= htlmFiles) {
			htmlIndex = htlmFiles;
			nextBt.setEnabled(false);
		} else {
			nextBt.setEnabled(true);
		}
		setProgress(htmlIndex*9999/htlmFiles);

		// Set html file and position
		String fileName = htmlList.get(htmlIndex - 1);
		if (anchor == null) {
			anchor = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.lastIndexOf("."));
		}
		showPage(fileName);
	}

	// Show htlm file
	void showPage(String htlmFile) {
		String url = "";
		boolean noImages;

		verifyWv.getSettings().setJavaScriptEnabled(false);
		verifyWv.clearView();

		// get html page
		StringBuilder htmlPageSb = new StringBuilder();
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(epubFile.getInputStream(epubFile.getEntry(htlmFile))), BUFFERSIZE);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				htmlPageSb.append(line);
			}
		} catch (IOException e) {
			readError();
		}

		// Set page colors
		String htmlPage = htmlPageSb.toString().replace("<body>", "<body bgcolor=\"Black\"><font color=\"White\">").replace("</body>", "</font></body>");

		// Check prefs
		if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("show_images", true)) {
			noImages = true;
		} else {
			noImages = false;

			// Remove old files
			removeFiles();

			// Save html page
			File file = new File(getFilesDir(), "page.html");
			url = file.getAbsolutePath();
			FileWriter writer;
			try {
				writer = new FileWriter(file);
				writer.append(htmlPage);
				writer.flush();
				writer.close();
			} catch (IOException e) {
				noImages = true;
			}

			if (!noImages) {
				// Get images
				XmlPullParserFactory factory;
				try {
					factory = XmlPullParserFactory.newInstance();
					XmlPullParser xpp = factory.newPullParser();

					xpp.setInput(new StringReader (htmlPage.replaceAll("&nbsp;", "")));
					int eventType = xpp.getEventType();

					// Search images in html file
					while (eventType != XmlPullParser.END_DOCUMENT) {
						if(eventType == XmlPullParser.START_TAG && "img".equals(xpp.getName())) {
							String imageName = xpp.getAttributeValue(null, "src");

							// Save image
							ZipEntry entry = epubFile.getEntry("OEBPS/" + imageName);
							BufferedInputStream in = new BufferedInputStream(epubFile.getInputStream(entry), BUFFERSIZE);
							FileOutputStream out = new FileOutputStream(new File(getFilesDir() + "/" + imageName));
							byte[] buffer = new byte[BUFFERSIZE];
							int len;
							BufferedOutputStream dest = new BufferedOutputStream(out, BUFFERSIZE);
							while ((len = in.read(buffer, 0, BUFFERSIZE)) != -1) {
								dest.write(buffer, 0, len);
							}
							dest.flush();
							dest.close();
							in.close();
						}
						eventType = xpp.next();
					}
				} catch (XmlPullParserException e) {
					System.err.println("XmlPullParserException in image preview");
				} catch (IOException e) {
					System.err.println("IOException in image preview");
				}

				// Show page with images
				verifyWv.clearCache(true);
				verifyWv.loadUrl("file://" + url);
			}
		}

		if (noImages) {
			// Show page without images
			verifyWv.loadDataWithBaseURL("app:html", htmlPage, "text/html", "utf-8", null);
		}
	}

	// Remove temp files
	private void removeFiles() {
		File file = new File(getFilesDir(), "");
		if (file != null && file.isDirectory()) {
			File[] files = file.listFiles();
			if(files != null) {
				for(File f : files) {   
					f.delete();
				}
			}
		}
	}

	// Prev button pressed
	View.OnClickListener mPrevListener = new OnClickListener() {
		public void onClick(View v) {
			changeHtmlFile(-1);
		}
	};

	// Next button pressed
	View.OnClickListener mNextListener = new OnClickListener() {
		public void onClick(View v) {
			changeHtmlFile(+1);
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

		htmlList = new ArrayList<String>();

		// open ePUB file
		try {
			epubFile = new ZipFile(filename);
		} catch (Exception e) {
			readError();
			return;
		}
		
			// Get page list
			Enumeration<? extends ZipEntry>entriesList;
			for (entriesList = epubFile.entries(); entriesList.hasMoreElements();) {
				ZipEntry entry = (ZipEntry) entriesList.nextElement();
				String name = entry.getName();
				if (name.endsWith(".html")) {
					htmlList.add(name);
				}
			}

			// Extract toc
			StringBuilder tocSb = new StringBuilder();
			try {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(epubFile.getInputStream(epubFile.getEntry("OEBPS/toc.ncx"))), BUFFERSIZE);
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					tocSb.append(line);
				}
			} catch (IOException e) {
				readError();
			}

			// Extract chapters
			XMLParser parser = new XMLParser();
			Document doc = parser.getDomElement(tocSb.toString());
			if (doc != null) {
				doc.normalize();
				NodeList nl = doc.getElementsByTagName("navPoint");
				if (nl != null) {
					// looping through all item nodes <item>
					for (int i = 0; i < nl.getLength(); i++) {
						Element e = (Element) nl.item(i);
						chapters.add(e.getTextContent().trim());
						NodeList nl2 = e.getChildNodes();
						anchors.add("OEBPS/" + parser.getValue((Element) nl2.item(3), "src"));
					}
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
		htmlIndex = -1;
		removeFiles();
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
		} catch (Exception e) {
			readError();
		}
	}
}
