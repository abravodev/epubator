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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfImageObject;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;

public class ReadPdf {
	private static PdfReader reader;
	private static HashMap<String, String> info;
	static List<String> imageList;

	// Open pdf file
	public static boolean open(String filename) {
		try {
			reader = new PdfReader(filename);
			info = reader.getInfo();
		} catch(Exception e) {
			return true;
		} catch(OutOfMemoryError e) {
			return true;
		} catch(NoClassDefFoundError e) {
			return true;
		}
		return false;
	}

	// Number of pages
	public static int getPages() {
		return reader.getNumberOfPages();
	}

	// Title
	public static String getTitle() {
		return info.get("Title");
	}

	// Author
	public static String getAuthor() {
		return info.get("Author");
	}

	// Extract text
	public static String extractText(int page) {
		try {
			return PdfTextExtractor.getTextFromPage(reader, page) + "\n";
		} catch(Exception e) {
			return "";
		}
	}

	// Extract images
	public static List<String> getImages(int page) {
		imageList = new ArrayList<String>();
		PdfReaderContentParser parser = new PdfReaderContentParser(reader);
		renderListener listener = new renderListener();
		try {
			parser.processContent(page, listener);
		} catch (IOException e) {
			System.err.println("Failed to extract image " + e.getMessage());
		}
		return imageList;
	}
}

// RenderListener helper class
class renderListener implements RenderListener {
	public void renderImage(ImageRenderInfo renderInfo) {
		try {
			// Get image
			PdfImageObject image = renderInfo.getImage();
			if (image != null) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				baos.write(image.getImageAsBytes());
				baos.flush();
				baos.close();

				// Check image type
				String imageType = image.getFileType();
				if (imageType == "png" || imageType == "gif" || imageType == "jpg") {
					if (imageType == "jpg") {
						imageType = "jpeg";
					}

					// Save to ePUB
					String imageName = String.format("image%s.%s", renderInfo.getRef().getNumber(), imageType);
					if (!WriteZip.addImage("OEBPS/" + imageName, baos.toByteArray())) {
						ReadPdf.imageList.add(imageName);
					}
				}

			}
		} catch (IOException e) {
			System.err.println("Failed to extract image (renderListener) " + e.getMessage());
		}
	}

	public void renderText(TextRenderInfo renderInfo) {
	}

	public void beginTextBlock() {
	}

	public void endTextBlock() {
	}
}

