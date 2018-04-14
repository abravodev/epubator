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

package it.iiizio.epubator.domain.utils;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.SimpleBookmark;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PdfReadHelper {

	private static PdfReader reader;
	private static HashMap<String, String> info;
	private static List<String> imageList;

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

	public static int getPages() {
		return reader.getNumberOfPages();
	}

	public static String getTitle() {
		String title = info.get("Title");
		if (title != null) {
			return title;
		}
		return "";
	}

	public static String getAuthor() {
		String author = info.get("Author");
		if (author != null) {
			return author;
		}
		return "";
	}

	public static String getPageText(int page) {
		try {
			return PdfTextExtractor.getTextFromPage(reader, page) + "\n";
		} catch(Exception e) {
			System.err.println("Failed to extract text " + e.getMessage());
			return "";
		} catch (OutOfMemoryError e) {
			System.err.println("Out of memory in text extraction " + e.getMessage());
			return "";
		}
	}

	// Extract images
	public static List<String> getPageImages(int page) {
		imageList = new ArrayList<>();
		PdfReaderContentParser parser = new PdfReaderContentParser(reader);
		ImageRenderListener listener = new ImageRenderListener();
		try {
			parser.processContent(page, listener);
		} catch (IOException e) {
			System.err.println("Failed to extract image " + e.getMessage());
		} catch (OutOfMemoryError e) {
			System.err.println("Out of memory in image extraction " + e.getMessage());
		} catch (RuntimeException e) {
			System.err.println("Runtime exception in image extraction " + e.getMessage());
		}
		return imageList;
	}

	public static String getBookmarks() {
		List<HashMap<String, Object>> list;
		try {
			list = SimpleBookmark.getBookmark(reader);
		} catch (ClassCastException e) {
			return "";
		}
		if (list == null) {
			return "";
		}
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
			SimpleBookmark.exportToXML(list, outputStream, "UTF-8", false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outputStream.toString();
	}

	public static void addImage(String image){
		imageList.add(image);
	}
}

