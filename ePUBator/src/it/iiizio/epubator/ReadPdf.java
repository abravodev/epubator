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

import java.util.HashMap;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

public class ReadPdf {
	private static PdfReader reader;
	private static HashMap<String, String> info;

	// Open pdf file
	public static boolean open(String filename) {
		try {
			reader = new PdfReader(filename);
			info = reader.getInfo();
		} catch(Exception e) {
			return true;
		} catch(OutOfMemoryError e) {
			return true;
		}
		return false;
	}

	// Extract text
	public static String extractText(int page) {
		try {
			return PdfTextExtractor.getTextFromPage(reader, page) + "\n";
		} catch(Exception e) {
			return "";
		}
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
}
