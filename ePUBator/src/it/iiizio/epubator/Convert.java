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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

public class Convert extends Activity {
	private static StringBuilder progressSb;
	private static ScrollView progressSv;
	private static TextView progressTv;
	private static Button okBt;
	private static Button stopBt;
	private static boolean okBtEnabled = true;
	private static boolean conversionStarted = false;
	static boolean notificationSent = false;
	private static int result;
	private static String filename = "";

	private final int pagesPerFile = 10;
	private final String PDF_EXT = ".pdf";
	private final String EPUB_EXT = " - ePUBator.epub";
	private final String TEMP_EXT = " - ePUBator.tmp";
	private final String OLD_EXT = " - ePUBator.old";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.progressview);

		// Set variables
		progressSv = (ScrollView)findViewById(R.id.scroll);
		progressTv = (TextView)findViewById(R.id.progress);
		okBt = (Button)findViewById(R.id.ok);
		okBt.setOnClickListener(mOkListener);
		stopBt = (Button)findViewById(R.id.stop);
		stopBt.setOnClickListener(mStopListener);

		if (conversionStarted) {
			// Update screen
			progressTv.setText(progressSb);
			setButtons(okBtEnabled);
		} else if (!notificationSent) {
			// Get filename
			Bundle extras = getIntent().getExtras();
			if (extras != null) {
				if (extras.containsKey("filename")) {
					String pdfFilename = extras.getString("filename");
					filename = pdfFilename.substring(0, pdfFilename.lastIndexOf(PDF_EXT));
					new convertTask().execute();
				}
			}
		}

		// Remove notification
		((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancel(R.string.app_name);
		notificationSent = false;
	}

	// Set buttons state
	private void setButtons(boolean flag) {
		okBtEnabled = flag;
		okBt.setEnabled(okBtEnabled);
		stopBt.setEnabled(!okBtEnabled);
	}

	// Ok button pressed
	private OnClickListener mOkListener = new OnClickListener()
	{
		public void onClick(View v)
		{
			conversionStarted = false;
			progressSb = null;
			progressSv = null;
			progressTv = null;
			finish();
		}
	};

	// Stop button pressed
	private OnClickListener mStopListener = new OnClickListener()
	{
		public void onClick(View v)
		{
			result = 5;
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
		return !okBtEnabled;
	}

	// Conversion started?
	public static boolean started() {
		return conversionStarted;
	}

	// Keep file dialog
	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == 0) {
			// Build dialog
			return new AlertDialog.Builder(Convert.this)
			.setTitle(getResources().getString(R.string.extaction_error))
			.setMessage(getResources().getString(R.string.keep))
			// Ok action
			.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					keepEpub();
				}
			})
			// Cancel action
			.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					progressSb.append("\n" + getResources().getStringArray(R.array.message)[4] + "\n");
					deleteTmp();
				}
			})
			// Preview action
			.setNeutralButton(getResources().getString(R.string.preview), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					Intent preview = new Intent(getBaseContext(), Preview.class);
					preview.putExtra("filename", filename + TEMP_EXT);
					startActivityForResult(preview, 0);
				}
			})
			.create();
		} else
			return null;
	}

	// Show dialog again after preview activity
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		showDialog(0);
	}

	// Delete file
	private void deleteTmp() {
		new File(filename + TEMP_EXT).delete();
		if (new File(filename + OLD_EXT).exists()) {
			new File(filename + OLD_EXT).renameTo(new File(filename + EPUB_EXT));
			progressSb.append(getResources().getString(R.string.kept_old));
		} else {
			progressSb.append(getResources().getString(R.string.deleted));
		}
		progressTv.setText(progressSb);
		scroll_up();
	}

	// Keep file
	private void keepEpub() {
		progressSb.append("\n" + getResources().getStringArray(R.array.message)[0] + "\n");
		progressSb.append(getResources().getString(R.string.errors) + "<<! page>>\n");
		progressSb.append(getResources().getString(R.string.page_lost) + "<<# page>>\n");
		renameFile();
		progressSb.append(getResources().getString(R.string.file) + " " + filename + EPUB_EXT);
		progressTv.setText(progressSb);
		scroll_up();
	}

	// Rename tmp file
	private void renameFile() {
		new File(filename + TEMP_EXT).renameTo(new File(filename + EPUB_EXT));
		new File(filename + OLD_EXT).delete();
	}

	// Scroll scroll view up
	private void scroll_up() {
		progressSv.post(new Runnable() {
			public void run() {
				progressSv.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});
	}

	// Send notification
	public void sendNotification() {
		NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, Convert.class), 0);
		String message = getResources().getStringArray(R.array.message)[result];
		String tickerText = getResources().getString(R.string.app_name);
		Notification notif = new Notification(R.drawable.ic_launcher, message, System.currentTimeMillis());
		notif.setLatestEventInfo(this, tickerText, message, contentIntent);
		nm.notify(R.string.app_name, notif);
		notificationSent = true;
	}

	// Start background task
	private class convertTask extends AsyncTask<Void, String, Void> {
		// Background task
		@Override
		protected Void doInBackground(Void... params) {
			// Remove bad files
			String path = filename.substring(0, filename.lastIndexOf('/', filename.length()) + 1);
			String[] files = new File(path).list();
			if(files.length > 0){
				for (int i = 0; i < files.length; i++) {
					if (files[i].endsWith(TEMP_EXT) || files[i].endsWith(OLD_EXT)) {
						new File(path + files[i]).delete();
					}
				}
			}

			// Save old ePUB
			if (new File(filename + EPUB_EXT).exists()) {
				new File(filename + EPUB_EXT).renameTo(new File(filename + OLD_EXT));
			}

			// Load PDF
			publishProgress(getResources().getString(R.string.load) + " " + filename + PDF_EXT);
			if (!(new File(filename + PDF_EXT).exists())) {
				// PDF file not found
				result = 1;
			} else if (ReadPdf.open(filename + PDF_EXT)) {
				// Failed to read PDF file
				result = 2;
			} else if (result != 5) {
				result = fillEpub(filename + TEMP_EXT);
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
			setButtons(false);
			result = 0;
			conversionStarted = true;
		}

		// Background task ended
		@Override
		protected void onPostExecute(Void params) {
			if (result == 4) {
				// Ask for keeping errored ePUB
				if (!isFinishing()) {
					showDialog(0);
				} else {
					keepEpub();
					result = 0;
				}
			} else {
				// Delete on failure
				publishProgress("\n" + getResources().getStringArray(R.array.message)[result]);
				if (result > 0) {
					deleteTmp();
				} else {
					// Keep if ok
					renameFile();
					publishProgress("\n" + getResources().getString(R.string.file) + " " + filename + EPUB_EXT);
				}
			}

			if (isFinishing()) {
				// Send notification
				sendNotification();
			}

			// Enable ok, disable stop
			setButtons(true);
		}

		// Fill ePUB file
		private int fillEpub(String filename) {
			try {
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
				if (WriteZip.addText("mimetype", "application/epub+zip", true)) {
					return 3;
				}

				publishProgress(getResources().getString(R.string.container));
				if (WriteZip.addText("META-INF/container.xml", createContainer(), false)) {
					return 3;
				}

				String bookId = filename.replaceAll("[^\\p{ASCII}]", "")+ " - " + new Date().hashCode();
				publishProgress(getResources().getString(R.string.content));
				if (WriteZip.addText("OEBPS/content.opf", createContent(pages, bookId), false)) {
					return 3;
				}

				publishProgress(getResources().getString(R.string.toc));
				if (WriteZip.addText("OEBPS/toc.ncx", createToc(pages, bookId), false)) {
					return 3;
				}

				publishProgress(getResources().getString(R.string.frontpage));
				if (WriteZip.addText("OEBPS/frontpage.html", createFrontpage(), false)) {
					return 3;
				}
				
				publishProgress(getResources().getString(R.string.frontpagepng));
				if (createFrontpagePng()) {
					return 3;
				}
				
				
				// Add extracted text
				for(int i = 1; i <= pages; i += pagesPerFile) {
					// Stopped?
					if (result == 5) {
						return 5;
					}

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

					if (WriteZip.addText("OEBPS/page" + i + ".html", createPages(i, textSb.toString()) , false)) {
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
			} catch(OutOfMemoryError e) {
				return 6;
			}
		}

		// Create container.xml
		private String createContainer() {
			StringBuilder container = new StringBuilder();
			container.append("<?xml version=\"1.0\"?>\n");
			container.append("<container version=\"1.0\" xmlns=\"urn:oasis:names:tc:opendocument:xmlns:container\">\n");
			container.append("   <rootfiles>\n");
			container.append("      <rootfile full-path=\"OEBPS/content.opf\" media-type=\"application/oebps-package+xml\"/>\n");
			container.append("   </rootfiles>\n");
			container.append("</container>\n");
			return container.toString();
		}

		// Create content.opf
		private String createContent(int pages, String id) {
			StringBuilder content = new StringBuilder();
			content.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			content.append("<package xmlns=\"http://www.idpf.org/2007/opf\" unique-identifier=\"BookID\" version=\"2.0\">\n");
			content.append("    <metadata xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:opf=\"http://www.idpf.org/2007/opf\">\n");
			content.append("        <dc:title>" + ReadPdf.getTitle() + "</dc:title>\n");
			content.append("        <dc:creator>" + ReadPdf.getAuthor() + "</dc:creator>\n");
			content.append("        <dc:creator opf:role=\"bkp\">ePUBator - Minimal offline PDF to ePUB converter for Android</dc:creator>\n");
			content.append("        <dc:identifier id=\"BookID\" opf:scheme=\"UUID\">" + id + "</dc:identifier>\n");
			content.append("        <dc:language>en</dc:language>\n");
			content.append("    </metadata>\n");
			content.append("    <manifest>\n");
			for(int i = 1; i <= pages; i += pagesPerFile) {
				content.append("        <item id=\"page" + i + "\" href=\"page" + i + ".html\" media-type=\"application/xhtml+xml\"/>\n");
			}
			content.append("        <item id=\"ncx\" href=\"toc.ncx\" media-type=\"application/x-dtbncx+xml\"/>\n");
			content.append("        <item id=\"frontpage\" href=\"frontpage.html\" media-type=\"application/xhtml+xml\"/>\n");
			content.append("        <item id=\"cover\" href=\"frontpage.png\" media-type=\"image/png\"/>\n");
			content.append("    </manifest>\n");
			content.append("    <spine toc=\"ncx\">\n");
			content.append("        <itemref idref=\"frontpage\"/>\n");
			for(int i = 1; i <= pages; i += pagesPerFile) {
				content.append("        <itemref idref=\"page" + i + "\"/>\n");
			}
			content.append("    </spine>\n");
			content.append("    <guide>\n");
			content.append("        <reference type=\"cover\" title=\"Frontpage\" href=\"frontpage.html\"/>\n");
			content.append("    </guide>\n");
			content.append("</package>\n");
			return content.toString();
		}

		// Create toc.ncx
		private String createToc(int pages, String id) {
			StringBuilder toc = new StringBuilder();
			toc.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			toc.append("<!DOCTYPE ncx PUBLIC \"-//NISO//DTD ncx 2005-1//EN\"\n");
			toc.append("   \"http://www.daisy.org/z3986/2005/ncx-2005-1.dtd\">\n");
			toc.append("<ncx xmlns=\"http://www.daisy.org/z3986/2005/ncx/\" version=\"2005-1\">\n");
			toc.append("    <head>\n");
			toc.append("        <meta name=\"dtb:uid\" content=\"" + id + "\"/>\n");
			toc.append("        <meta name=\"dtb:depth\" content=\"1\"/>\n");
			toc.append("        <meta name=\"dtb:totalPageCount\" content=\"0\"/>\n");
			toc.append("        <meta name=\"dtb:maxPageNumber\" content=\"0\"/>\n");
			toc.append("    </head>\n");
			toc.append("    <docTitle>\n");
			toc.append("        <text>" + ReadPdf.getTitle() + "</text>\n");
			toc.append("    </docTitle>\n");
			toc.append("    <navMap>\n");
			toc.append("        <navPoint id=\"navPoint-1\" playOrder=\"1\">\n");
			toc.append("            <navLabel>\n");
			toc.append("                <text>Frontpage</text>\n");
			toc.append("            </navLabel>\n");
			toc.append("            <content src=\"frontpage.html\"/>\n");
			toc.append("        </navPoint>\n");
			int playOrder = 2;
			for(int i = 1; i <= pages; i += pagesPerFile) {
				toc.append("        <navPoint id=\"navPoint-" + playOrder + "\" playOrder=\"" + playOrder + "\">\n");
				toc.append("            <navLabel>\n");
				toc.append("                <text>Page" + i + "</text>\n");
				toc.append("            </navLabel>\n");
				toc.append("            <content src=\"page" + i + ".html\"/>\n");
				toc.append("        </navPoint>\n");
				playOrder += 1;
			}
			toc.append("    </navMap>\n");
			toc.append("</ncx>\n");
			return toc.toString();
		}

		// Create html
		private String createHtml(String title, String body) {
			StringBuilder html = new StringBuilder();
			html.append("  <!DOCTYPE html PUBLIC \"-//WAPFORUM//DTD XHTML Mobile 1.0//EN\" \n");
			html.append("  \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n");
			html.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
			html.append("<head>\n");
			html.append("  <meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"/>\n");
			html.append("  <meta name=\"generator\" content=\"ePUBator - Minimal offline PDF to ePUB converter for Android\"/>\n");
			html.append("  <title>" + title + "</title>\n");
			html.append("</head>\n");
			html.append("<body>\n");
			html.append(body);
			html.append("</body>\n");
			html.append("</html>\n");
			return html.toString();
		}

		// Create pageNN.html
		private String createPages(int offset, String text) {
			StringBuilder body = new StringBuilder();
			body.append("  <p>\n");
			body.append(text.replaceAll("<br/>(?=[a-z])", "&nbsp;"));
			body.append("  </p>\n");
			return createHtml("page" + offset, body.toString());
		}

		// Create frontpage.html
		private String createFrontpage() {
			StringBuilder body = new StringBuilder();
			body.append("  <svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" version=\"1.1\" width=\"100%\" height=\"100%\" viewBox=\"0 0 838 1186\">\n");
			body.append("    <image width=\"720\" height=\"1080\" xlink:href=\"frontpage.png\"/>\n");
			body.append("  </svg>\n");
			return createHtml("Frontpage", body.toString());
		}

		// Create frontpage.png
		private boolean createFrontpagePng() {
	        Paint paint  = new Paint();
	        int border = 10;
	        int fontsize = 36;
	        
	        Bitmap bmp = Bitmap.createBitmap(240, 360, Bitmap.Config.ARGB_8888);
	        Canvas canvas = new Canvas(bmp);
	        int maxWidth = canvas.getWidth();
	        int maxHeight = canvas.getHeight();
	        paint.setColor(Color.LTGRAY);
	        canvas.drawRect(0, 0, maxWidth, maxHeight, paint);
	        
	        Bitmap img = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
	        canvas.drawBitmap(img, maxWidth - img.getWidth(), maxHeight - img.getHeight(), new Paint(Paint.FILTER_BITMAP_FLAG));
   
	        paint.setTextSize(fontsize);
	        paint.setColor(Color.BLACK);
	        paint.setAntiAlias(true);
	        paint.setStyle(Paint.Style.FILL);
	        
	        String name = filename.substring(filename.lastIndexOf("/") + 1, filename.length());
/*			if (ReadPdf.getTitle() != null) {
	        	name = ReadPdf.getTitle() + " - " + ReadPdf.getAuthor();
	        }*/
			name = name.replaceAll("_", " "); //.replaceAll("\\[.*?\\]","");
	        String words[] = name.split("\\s");
	        
	        float newline = paint.getFontSpacing();
	        float x = border;
	        float y = newline;

	        for (String word : words) {
	        	float len = paint.measureText(word + " ");

	        	if ((x > border) && ((x + len) > maxWidth)) {
	        		x = border;
	        		y += newline;
	        	}

	        	canvas.drawText(word, x, y, paint);
	        	x += len;
	        }

	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
	        return WriteZip.addImage("OEBPS/frontpage.png", baos.toByteArray());
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
