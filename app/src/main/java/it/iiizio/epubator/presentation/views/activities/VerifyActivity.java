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
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;

import it.iiizio.epubator.R;
import it.iiizio.epubator.domain.constants.BundleKeys;
import it.iiizio.epubator.domain.entities.EBook;
import it.iiizio.epubator.domain.exceptions.NotFoundException;
import it.iiizio.epubator.domain.services.EpubService;
import it.iiizio.epubator.domain.services.EpubServiceImpl;
import it.iiizio.epubator.domain.utils.PdfXmlParserImpl;
import it.iiizio.epubator.infrastructure.providers.SharedPreferenceProviderImpl;
import it.iiizio.epubator.infrastructure.providers.StorageProvider;
import it.iiizio.epubator.infrastructure.providers.StorageProviderImpl;
import it.iiizio.epubator.presentation.presenters.VerifyPresenter;
import it.iiizio.epubator.presentation.presenters.VerifyPresenterImpl;

public class VerifyActivity extends AppCompatActivity {

	//<editor-fold desc="Attributes">
	private static final int DEFAULT_FIRST_PAGE = 1;
	private static int currentPageIndex = DEFAULT_FIRST_PAGE;
	private EBook book;
	private String anchor;

	private WebView wv_verifyEpub;
	private ProgressBar pb_verify_epub;
	private Button bt_previousPage;
	private Button bt_nextPage;

	private VerifyPresenter presenter;
	//</editor-fold>

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_verify);

		presenter = makePresenter();

		setupElements();
		setupBook();

		gotoPage(currentPageIndex);
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

	@Override
	public void onBackPressed() {
		exitEpubVerification();
	}

	private VerifyPresenter makePresenter(){
		StorageProvider storageProvider = new StorageProviderImpl(this);
		EpubService epubService = new EpubServiceImpl(storageProvider, new PdfXmlParserImpl());
		return new VerifyPresenterImpl(epubService, new SharedPreferenceProviderImpl(this), storageProvider);
	}

	private void setupElements() {
		setupWebView();
		setupProgressBar();
		setupPageButtons();
	}

	private void setupProgressBar() {
		pb_verify_epub = (ProgressBar) findViewById(R.id.pb_verify_epub);
	}

	private void updateProgressBar(int currentValue, int maxValue){
		pb_verify_epub.setMax(maxValue);
		updateProgressBar(currentValue);
	}

	private void updateProgressBar(int currentValue){
		pb_verify_epub.setProgress(currentValue);
	}

	private void openIndexDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(VerifyActivity.this);
		builder.setTitle(R.string.index)
				.setItems(book.getChapters(), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						int pageIndex = book.getPageIndex(which);
						anchor = book.getAnchor(which);
						if(book.isValidPage(pageIndex)){
							gotoPage(pageIndex);
						} else {
							// TODO: Show message (page not valid)
						}
					}
				})
				.create()
				.show();
	}

	private void setupPageButtons() {
		bt_previousPage = (Button) findViewById(R.id.bt_previous_page);
		bt_nextPage = (Button) findViewById(R.id.bt_next_page);

		OnClickListener pageListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(v.getId() == R.id.bt_previous_page){
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
		wv_verifyEpub = (WebView) findViewById(R.id.wv_verify_epub);
		wv_verifyEpub.setBackgroundColor(0);
		wv_verifyEpub.setWebViewClient(new WebViewClient() {
			public void onPageFinished(final WebView view, final String url) {
				// make it jump to the internal link
				if (anchor != null) {
					view.loadUrl(url + "#" + anchor);
					anchor = null;
				}
			}
		});

		WebSettings webSettings = wv_verifyEpub.getSettings();
		webSettings.setJavaScriptEnabled(false);
		webSettings.setUseWideViewPort(true);
		webSettings.setLoadWithOverviewMode(true);
		webSettings.setSupportZoom(true);
		webSettings.setBuiltInZoomControls(true);
	}

	private void setupBook() {
		String filename = getIntent().getStringExtra(BundleKeys.FILENAME);

		try {
			book = presenter.getBook(filename);
			updateProgressBar(1, book.getPagesCount());
		} catch (IOException e) {
			exitEpubVerificationOnError();
		}
	}

	private void gotoPreviousPage(){
		if(book.hasPreviousPage(currentPageIndex)){
			gotoPage(currentPageIndex - 1);
		}
	}

	private void gotoNextPage(){
		if(book.hasNextPage(currentPageIndex)){
			gotoPage(currentPageIndex + 1);
		}
	}

	private void gotoPage(int pageIndex){
		currentPageIndex = pageIndex;
		bt_previousPage.setEnabled(book.hasPreviousPage(currentPageIndex));
		bt_nextPage.setEnabled(book.hasNextPage(currentPageIndex));
		updateProgressBar(pageIndex);

		try {
			String fileName = book.getPage(currentPageIndex);
			if (anchor == null) {
				anchor = book.getAnchorFromPageName(currentPageIndex);
			}
			showPage(fileName);
		} catch (NotFoundException e) {
			// TODO: Show toast error
			System.err.println(e);
		}

	}

	private void showPage(String htmlFile) {
		wv_verifyEpub.clearView();

		String htmlPage;
		try {
			htmlPage = presenter.getHtmlPage(book.getFile(), htmlFile);
		} catch (IOException e) {
			exitEpubVerificationOnError();
			return;
		}

		boolean showImages = presenter.showImages();
		if(showImages) {
			try {
				String url = presenter.saveHtmlPage(book.getFile(), htmlPage);
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

	private void exitEpubVerificationOnError() {
		Toast.makeText(getApplicationContext(), getResources().getString(R.string.cannot_read_file), Toast.LENGTH_SHORT).show();
		exitEpubVerification();
	}

	private void exitEpubVerification() {
		currentPageIndex = DEFAULT_FIRST_PAGE;
		closeEpub();
		presenter.removeFilesFromTemporalDirectory();
		finish();
	}

	private void closeEpub() {
		try {
			presenter.closeBook(book);
		} catch (IOException e) {
			// TODO: Show message (book could not be closed)
		}
	}
}
