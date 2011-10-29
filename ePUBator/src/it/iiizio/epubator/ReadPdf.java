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
		}
		return false;
	}

	// Extract text
	public static String extractText(int startPage, int endPage) {
		String text = "";

		if (endPage > reader.getNumberOfPages()) {
			endPage = reader.getNumberOfPages();
		}

		try {
			for (int i = startPage; i <= endPage; i++) {
				text += PdfTextExtractor.getTextFromPage(reader,i) + "\n";
			}
		} catch(Exception e) {
			return "";
		}
		return stringToHTMLString(text);
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

	//  stringToHTMLString found on the web, no license indicated
	//  http://www.rgagnon.com/javadetails/java-0306.html
	//	Author: S. Bayer.
	private static String stringToHTMLString(String string) {
		StringBuffer sb = new StringBuffer(string.length());
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
