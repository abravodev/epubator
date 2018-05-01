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
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

import it.iiizio.epubator.R;
import it.iiizio.epubator.domain.constants.BundleKeys;
import it.iiizio.epubator.domain.constants.PreferencesKeys;
import it.iiizio.epubator.domain.entities.Book;
import it.iiizio.epubator.domain.utils.FileHelper;
import it.iiizio.epubator.presentation.presenters.VerifyPresenter;
import it.iiizio.epubator.presentation.presenters.VerifyPresenterImpl;
import it.iiizio.epubator.presentation.utils.BundleHelper;
import it.iiizio.epubator.presentation.utils.PreferencesHelper;

public class VerifyActivity extends AppCompatActivity {

	private static ZipFile epubFile = null;
	private static int currentPageIndex = 1;
	private Book book;
	private String anchor;

	private WebView wv_verifyEpub;
	private Button bt_previousPage;
	private Button bt_nextPage;

	private VerifyPresenter presenter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_verify);

		presenter = new VerifyPresenterImpl();

		setupElements();
		setupBook();

		gotoPage(currentPageIndex);
	}

	private void setupElements() {
		setupWebView();
		setupPageButtons();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.verifymenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.index){
			openIndexDialog();
		}
		return true;
	}

	private void openIndexDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(VerifyActivity.this);
		builder.setTitle(R.string.index)
			.setItems(book.getChapters(), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						int pageIndex = book.getPageIndex(which)+1;
						anchor = book.getAnchor(which);
						if(!validPage(pageIndex)){
							// TODO: Show message (page not valid)
							return;
						}
						gotoPage(pageIndex);
					}
			})
			.create()
			.show();
	}

	@Override
	public void onBackPressed() {
		exitEpubVerification();
	}

	private void setupPageButtons() {
		bt_previousPage = (Button) findViewById(R.id.previous_page_button);
		bt_nextPage = (Button) findViewById(R.id.next_page_button);

		OnClickListener pageListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(v.getId() == R.id.previous_page_button){
					gotoPreviousPage();
				} else {
					gotoNextPage();
				}
			}
		};

		bt_previousPage.setOnClickListener(pageListener);
		bt_nextPage.setOnClickListener(pageListener);
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

	private void setupBook() {
		String filename = BundleHelper.getExtraStringOrDefault(getIntent(), BundleKeys.FILENAME);

		try {
			epubFile = new ZipFile(filename);
			book = presenter.getBook(epubFile);
			if(book.getPagesCount()==0){
				throw new IOException("Book has no pages");
			}
		} catch (IOException e) {
			exitEpubVerificationOnError();
		}
	}

	private void gotoPreviousPage(){
		if(book.hasPreviousPage(currentPageIndex)){
			gotoPage(currentPageIndex -1);
		}
	}

	private void gotoNextPage(){
		if(book.hasNextPage(currentPageIndex)){
			gotoPage(currentPageIndex +1);
		}
	}

	private void gotoPage(int pageIndex){
		currentPageIndex = pageIndex;
		bt_previousPage.setEnabled(book.hasPreviousPage(currentPageIndex));
		bt_nextPage.setEnabled(book.hasNextPage(currentPageIndex));

		String fileName = book.getPage(currentPageIndex);
		if (anchor == null) {
			anchor = book.getAnchorFromPageName(currentPageIndex);
		}
		showPage(fileName);
	}

	private boolean validPage(int pageIndex){
		return 1 <= pageIndex && pageIndex <= book.getPagesCount();
	}

	private void showPage(String htmlFile) {
		wv_verifyEpub.getSettings().setJavaScriptEnabled(false);
		wv_verifyEpub.clearView();

		String htmlPage = null;
		try {
			htmlPage = presenter.getHtmlPage(epubFile, htmlFile);
		} catch (IOException e) {
			exitEpubVerificationOnError();
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
			wv_verifyEpub.loadDataWithBaseURL("app:html", htmlPage, "text/html", "utf-8", null);
		}
	}

	private boolean showImages(){
		return new PreferencesHelper(this).getBoolean(PreferencesKeys.SHOW_IMAGES_ON_VERIFY, true);
	}

	private void removeFiles() {
		File directory = new File(getFilesDir(), "");
		FileHelper.deleteFilesFromDirectory(directory);
	}

	private void exitEpubVerificationOnError() {
		Toast.makeText(getApplicationContext(), getResources().getString(R.string.cannot_read_file), Toast.LENGTH_SHORT).show();
		exitEpubVerification();
	}

	private void exitEpubVerification() {
		closeEpub();
		removeFiles();
		finish();
	}

	private void closeEpub() {
		try {
			epubFile.close();
		} catch (Exception e) {
			// TODO: Show message (book could not be closed)
		}
	}
}
