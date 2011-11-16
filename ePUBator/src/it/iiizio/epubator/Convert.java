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
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

public class Convert extends Activity {
	private static StringBuilder progressSb;
	private static TextView progressTv;
	private static Button okBt;
	private static boolean backBtEnabled = true;
	private static boolean conversionStarted = false;

	private int pagesPerFile = 10;
	private static int result;
	private static String BookId;
	private static String epubFilename;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.progressview);

		progressTv = ((TextView)findViewById(R.id.progress));
		okBt = (Button)findViewById(R.id.back);
		okBt.setOnClickListener(mBackListener);

		if (conversionStarted) {
			// Conversion already started, update screen
			progressTv.setText(progressSb);
			okBt.setEnabled(backBtEnabled);
		} else {
			// Start conversion
			new convertTask().execute();
		}
	}

	// Ok button pressed
	private OnClickListener mBackListener = new OnClickListener()
	{
		public void onClick(View v)
		{
			conversionStarted = false;
			finish();
		}
	};

	// Back button pressed
	@Override
	public void onBackPressed() {
		conversionStarted = working();
		finish();
	}

	// Conversion in progress?
	public static boolean working() {
		return !backBtEnabled;
	}

	// Conversion started?
	public static boolean started() {
		return conversionStarted;
	}

	// Keep file dialog
	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == 0) {
			return new AlertDialog.Builder(Convert.this)
			.setTitle(getResources().getString(R.string.extactionerror))
			.setMessage(getResources().getString(R.string.keep))
			.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					keepEpub();
				}
			})
			.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					progressSb.append("\n" + getResources().getStringArray(R.array.message)[4] + "\n");
					deleteEpub();
					progressTv.setText(progressSb);
					scroll_up();
				}
			})
			.create();
		} else
			return null;
	}

	// Delete file
	private void deleteEpub() {
		new File(epubFilename).delete();
		progressSb.append(getResources().getString(R.string.deleted));
	}

	// Keep file
	private void keepEpub() {
		progressSb.append("\n" + getResources().getStringArray(R.array.message)[0] + "\n");
		progressSb.append(getResources().getString(R.string.pagelost) + "<<# page>>\n");
		progressSb.append(getResources().getString(R.string.errors) + "<<! page>>\n");
		progressSb.append(getResources().getString(R.string.file) + " " + epubFilename);
		progressTv.setText(progressSb);
		scroll_up();
	}

	// Scroll scroll view up
	private void scroll_up() {
		((ScrollView)findViewById(R.id.scroll)).post(new Runnable() {
			public void run() {
				((ScrollView)findViewById(R.id.scroll)).fullScroll(ScrollView.FOCUS_DOWN);
			}
		});
	}

	// Start background task
	private class convertTask extends AsyncTask<Void, String, Void> {
		// Background task
		@Override
		protected Void doInBackground(Void... params) {
			// Get filename
			String pdfFilename = "";
			Bundle extras = getIntent().getExtras();
			if (extras != null) {
				if (extras.containsKey("filename")) {
					pdfFilename = extras.getString("filename");
				}
			}
			publishProgress(getResources().getString(R.string.load) + " " + pdfFilename);

			File pdfFile = new File(pdfFilename);
			BookId = pdfFile.getName().substring(0, pdfFile.getName().lastIndexOf(".pdf")).replaceAll("[^\\p{ASCII}]", "")+ " - " + new Date().hashCode();
			epubFilename = pdfFilename.substring(0, pdfFilename.lastIndexOf(".pdf")) + " - ePUBator.epub";

			if (!pdfFile.exists()) {
				// PDF file not found
				result = 1;
			} else if (ReadPdf.open(pdfFilename)) {
				// Failed to read PDF file
				result = 2;
			} else {
				result = fillEpub(epubFilename);
			}

			return null;
		}

		// Update screen
		@Override
		protected void onProgressUpdate(String... messageArray) {
			for (String message : messageArray)
			{
				progressSb.append(message + "\n");
			}
			progressTv.setText(progressSb);
			scroll_up();
		}

		// Prepare background task
		@Override
		protected void onPreExecute() {
			progressSb = new StringBuilder();
			progressSb.append(getResources().getString(R.string.heading));
			okBt.setEnabled(backBtEnabled = false);
			conversionStarted = true;	
		}

		// Background task ended
		@Override
		protected void onPostExecute(Void params) {
			if (result == 4) {
				if (!isFinishing()) {
					showDialog(0);
				} else {
					keepEpub();
				}
			} else {
				publishProgress("\n" + getResources().getStringArray(R.array.message)[result]);
				if (result > 0) {
					deleteEpub();
				} else {
					publishProgress(getResources().getString(R.string.file) + epubFilename);
				}
			}
			okBt.setEnabled(backBtEnabled = true);
		}

		// Fill ePUB file
		private int fillEpub(String filename) {
			// Set up counter
			int pages = ReadPdf.getPages();
			publishProgress(getResources().getString(R.string.pages) + " " + pages);
			int totalFiles = 1 + pages / pagesPerFile;
			int writedFiles = 0;

			// Set flag
			boolean extractionErrorFlag = false;

			// Create ePUB file
			publishProgress(getResources().getString(R.string.open));
			if (WriteZip.create(filename)) {
				return 3;
			}

			// Add required files
			publishProgress(getResources().getString(R.string.mimetype));
			if (WriteZip.addEntry("mimetype", "application/epub+zip", true)) {
				return 3;
			}

			publishProgress(getResources().getString(R.string.container));
			if (WriteZip.addEntry("META-INF/container.xml", createContainer(), false)) {
				return 3;
			}

			publishProgress(getResources().getString(R.string.content));
			if (WriteZip.addEntry("OEBPS/content.opf", createContent(pages), false)) {
				return 3;
			}

			publishProgress(getResources().getString(R.string.toc));
			if (WriteZip.addEntry("OEBPS/toc.ncx", createToc(pages), false)) {
				return 3;
			}

			// Add extracted text
			for(int i = 1; i <= pages; i += pagesPerFile) {
				StringBuilder textSb = new StringBuilder();

				publishProgress(getResources().getString(R.string.html) + i + ".html   " + (int)(100 * (++writedFiles) / totalFiles) + "%");
				int endPage = i + pagesPerFile - 1;
				if (endPage > pages) {
					endPage = pages;
				}

				for (int j = i; j <= endPage; j++) {
					String page = stringToHTMLString(ReadPdf.extractText(j));
					if (page.length() == 0) {
						textSb.append("&lt;&lt;# " + j + "&gt;&gt;");
						extractionErrorFlag = true;
					} else {
						if (page.matches(".*\\p{Cntrl}.*")) {
							textSb.append(page.replaceAll("\\p{Cntrl}+", "&lt;&lt;! " + j + "&gt;&gt;"));
							extractionErrorFlag = true;
						} else {
							textSb.append(page);
						}
					}
				}

				if (WriteZip.addEntry("OEBPS/page" + i + ".html", createPages(i, textSb.toString()) , false)) {
					return 3;
				}
			}

			// Close ePUB file
			publishProgress(getResources().getString(R.string.close));
			if (WriteZip.close()) {
				return 3;
			}

			if (extractionErrorFlag) {
				return 4;
			} else {
				return 0;
			}
		}

		// Create container.xml
		private String createContainer() {
			String container = "<?xml version=\"1.0\"?>\n";
			container += "<container version=\"1.0\" xmlns=\"urn:oasis:names:tc:opendocument:xmlns:container\">\n";
			container += "   <rootfiles>\n";
			container += "      <rootfile full-path=\"OEBPS/content.opf\" media-type=\"application/oebps-package+xml\"/>\n";
			container += "   </rootfiles>\n";
			container += "</container>\n";
			return container;
		}

		// Create content.opf
		private String createContent(int pages) {
			String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
			content += "<package xmlns=\"http://www.idpf.org/2007/opf\" unique-identifier=\"BookID\" version=\"2.0\">\n";
			content += "    <metadata xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:opf=\"http://www.idpf.org/2007/opf\">\n";
			content += "        <dc:title>" + ReadPdf.getTitle() + "</dc:title>\n";
			content += "        <dc:creator>" + ReadPdf.getAuthor() + "</dc:creator>\n";
			content += "        <dc:creator opf:role=\"bkp\">ePUBator - Minimal offline PDF to ePUB converter for Android</dc:creator>\n";
			content += "        <dc:identifier id=\"BookID\" opf:scheme=\"UUID\">" + BookId + "</dc:identifier>\n";
			content += "        <dc:language>en</dc:language>\n";
			content += "    </metadata>\n";
			content += "    <manifest>\n";
			for(int i = 1; i <= pages; i += pagesPerFile) {
				content += "        <item id=\"page" + i + "\" href=\"page" + i + ".html\" media-type=\"application/xhtml+xml\"/>\n";
			}
			content += "        <item id=\"ncx\" href=\"toc.ncx\" media-type=\"application/x-dtbncx+xml\"/>\n";
			content += "    </manifest>\n";
			content += "    <spine toc=\"ncx\">\n";
			for(int i = 1; i <= pages; i += pagesPerFile) {
				content += "        <itemref idref=\"page" + i + "\"/>\n";
			}
			content += "    </spine>\n";
			content += "</package>\n";
			return content;
		}

		// Create toc.ncx
		private String createToc(int pages) {
			String toc = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
			toc += "<!DOCTYPE ncx PUBLIC \"-//NISO//DTD ncx 2005-1//EN\"\n";
			toc += "   \"http://www.daisy.org/z3986/2005/ncx-2005-1.dtd\">\n";
			toc += "<ncx xmlns=\"http://www.daisy.org/z3986/2005/ncx/\" version=\"2005-1\">\n";
			toc += "    <head>\n";
			toc += "        <meta name=\"dtb:uid\" content=\"" + BookId + "\"/>\n";
			toc += "        <meta name=\"dtb:depth\" content=\"1\"/>\n";
			toc += "        <meta name=\"dtb:totalPageCount\" content=\"0\"/>\n";
			toc += "        <meta name=\"dtb:maxPageNumber\" content=\"0\"/>\n";
			toc += "    </head>\n";
			toc += "    <docTitle>\n";
			toc += "        <text>" + ReadPdf.getTitle() + "</text>\n";
			toc += "    </docTitle>\n";
			toc += "    <navMap>\n";
			int playOrder = 1;
			for(int i = 1; i <= pages; i += pagesPerFile) {
				toc += "        <navPoint id=\"navPoint-" + playOrder + "\" playOrder=\"" + playOrder + "\">\n";
				toc += "            <navLabel>\n";
				toc += "                <text>Page" + i + "</text>\n";
				toc += "            </navLabel>\n";
				toc += "            <content src=\"page" + i + ".html\"/>\n";
				toc += "        </navPoint>\n";
				playOrder += 1;
			}
			toc += "    </navMap>\n";
			toc += "</ncx>\n";
			return toc;
		}

		// Create pageNN.html
		private String createPages(int offset, String text) {
			String html = "  <!DOCTYPE html PUBLIC \"-//WAPFORUM//DTD XHTML Mobile 1.0//EN\" \n";
			html += "  \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n";
			html += "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n";
			html += "<head>\n";
			html += "  <meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"/>\n";
			html += "  <meta name=\"generator\" content=\"ePUBator - Minimal offline PDF to ePUB converter for Android\"/>\n";
			html += "  <title>page" + offset + "</title>\n";
			html += "</head>\n";
			html += "<body>\n";
			html += "  <p>\n";
			html += text.replaceAll("<br/>(?=[a-z])", "&nbsp;");
			html += "  </p>\n";
			html += "</body>\n";
			html += "</html>\n";
			return html;
		}

		//  stringToHTMLString found on the web, no license indicated
		//  http://www.rgagnon.com/javadetails/java-0306.html
		//	Author: S. Bayer.
		private  String stringToHTMLString(String string) {
			StringBuilder sb = new StringBuilder(); // changed StringBuffer to StringBuilder to prevent buffer overflow
			// true if last char was blank
			boolean lastWasBlankChar = false;
			int len = string.length();
			char c;

			for (int i = 0; i < len; i++)
			{
				c = string.charAt(i);
				if (c == ' ') {
					// blank gets extra work,
					// this solves the problem you get if you replace all
					// blanks with &nbsp;, if you do that you loss 
					// word breaking
					if (lastWasBlankChar) {
						lastWasBlankChar = false;
						sb.append("&nbsp;");
					}
					else {
						lastWasBlankChar = true;
						sb.append(' ');
					}
				}
				else {
					lastWasBlankChar = false;
					//
					// HTML Special Chars
					if (c == '"')
						sb.append("&quot;");
					else if (c == '&')
						sb.append("&amp;");
					else if (c == '<')
						sb.append("&lt;");
					else if (c == '>')
						sb.append("&gt;");
					else if (c == '\n')
						// Handle Newline
						sb.append("<br/>");
					else {
						int ci = 0xffff & c;
						if (ci < 160 )
							// nothing special only 7 Bit
							sb.append(c);
						else {
							// Not 7 Bit use the unicode system
							sb.append("&#");
							sb.append(new Integer(ci).toString());
							sb.append(';');
						}
					}
				}
			}
			return sb.toString();
		}
	}
}
