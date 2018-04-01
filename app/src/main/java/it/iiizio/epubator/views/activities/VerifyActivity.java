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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

import it.iiizio.epubator.R;
import it.iiizio.epubator.model.constants.BundleKeys;
import it.iiizio.epubator.model.constants.PreferencesKeys;
import it.iiizio.epubator.model.entities.Book;
import it.iiizio.epubator.model.utils.FileHelper;
import it.iiizio.epubator.presenters.VerifyPresenter;
import it.iiizio.epubator.presenters.VerifyPresenterImpl;

public class VerifyActivity extends Activity {

	private static ZipFile epubFile = null;
	private List<String> htmlList;
	private static int htmlIndex = -1;
	private List<String> chapters = new ArrayList<>();
	private List<String> anchors = new ArrayList<>();
	private WebView wv_verifyEpub;
	private Button bt_previousPage;
	private Button bt_nextPage;
	private String anchor;

	private VerifyPresenter presenter;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		setProgressBarVisibility(true);
		setContentView(R.layout.verify);
		presenter = new VerifyPresenterImpl();

		setupWebView();
		setupPageButtons();

		// Initialize
		fillPageList();
		if (htmlIndex == -1) {
			htmlIndex = 1;
		}

		changeHtmlFile(0);
	}

	private void setupPageButtons() {
		bt_previousPage = (Button) findViewById(R.id.prev);
		bt_nextPage = (Button) findViewById(R.id.next);

		bt_previousPage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				changeHtmlFile(-1);
			}
		});

		bt_nextPage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				changeHtmlFile(+1);
			}
		});
	}

	private void setupWebView() {
		wv_verifyEpub = (WebView) findViewById(R.id.webview);
		wv_verifyEpub.setBackgroundColor(0);
		wv_verifyEpub.getSettings().setUseWideViewPort(true);
		wv_verifyEpub.setWebViewClient(new WebViewClient() {
			public void onPageFinished(final WebView view, final String url) {
				// make it jump to the internal link
				if (anchor != null) {
					view.loadUrl(url + "#" + anchor);
					anchor = null;
				}
			}
		});
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.verifymenu, menu);
		MenuItemCompat.setShowAsAction(menu.findItem(R.id.index), 1);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.index:
				showDialog(0);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(VerifyActivity.this);
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
			bt_previousPage.setEnabled(false);
		} else {
			bt_previousPage.setEnabled(true);
		}
		if  (htmlIndex >= htlmFiles) {
			htmlIndex = htlmFiles;
			bt_nextPage.setEnabled(false);
		} else {
			bt_nextPage.setEnabled(true);
		}
		setProgress(htmlIndex*9999/htlmFiles);

		// Set html file and position
		String fileName = htmlList.get(htmlIndex - 1);
		if (anchor == null) {
			anchor = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.lastIndexOf("."));
		}
		showPage(fileName);
	}

	void showPage(String htmlFile) {
		wv_verifyEpub.getSettings().setJavaScriptEnabled(false);
		wv_verifyEpub.clearView();

		String htmlPage = null;
		try {
			htmlPage = presenter.getHtmlPage(epubFile, htmlFile);
		} catch (IOException e) {
			readError();
		}

		boolean showImages = showImages();
		if(showImages) {
			removeFiles();

			File outputDirectory = new File(getFilesDir(), "page.html");

			try {
				presenter.saveHtmlPage(outputDirectory, htmlPage);
				presenter.saveImages(epubFile, htmlPage, getFilesDir());

				String url = outputDirectory.getAbsolutePath();
				wv_verifyEpub.clearCache(true);
				wv_verifyEpub.loadUrl("file://" + url);
			} catch (IOException e) {
				showImages = false;
			}
		}

		if (!showImages) {
			// Show page without images
			wv_verifyEpub.loadDataWithBaseURL("app:html", htmlPage, "text/html", "utf-8", null);
		}
	}

	private boolean showImages(){
		return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PreferencesKeys.SHOW_IMAGES_ON_VERIFY, true);
	}

	private void removeFiles() {
		File directory = new File(getFilesDir(), "");
		FileHelper.deleteFilesFromDirectory(directory);
	}

	private void fillPageList() {
		String filename = getFilename();

		try {
			epubFile = new ZipFile(filename);
		} catch (Exception e) {
			readError();
			return;
		}

		htmlList = presenter.getPages(epubFile);

		try {
			Book book = presenter.getBook(epubFile);
			chapters = book.getChapters();
			anchors = book.getAnchors();
		} catch (IOException e) {
			readError();
		}
	}

	private String getFilename(){
		Bundle extras = getIntent().getExtras();
		if (extras != null && extras.containsKey(BundleKeys.FILENAME)) {
			return extras.getString(BundleKeys.FILENAME);
		}

		return "";
	}

	private void readError() {
		Toast.makeText(getApplicationContext(), getResources().getString(R.string.cannot_read_file), Toast.LENGTH_SHORT).show();
		exit();
	}

	private void exit() {
		htmlIndex = -1;
		removeFiles();
		finish();
	}

	@Override
	public void onBackPressed() {
		closeEpub();
		exit();
	}

	private void closeEpub() {
		try {
			epubFile.close();
		} catch (Exception e) {
			readError();
		}
	}
}
