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

package it.iiizio.epubator.domain.services;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.SimpleBookmark;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.iiizio.epubator.domain.callbacks.ImageRenderedCallback;
import it.iiizio.epubator.domain.utils.ImageRenderListener;

public class PdfReaderServiceImpl implements PdfReaderService {

	//<editor-fold desc="Attributes">
	private final List<String> imageList;
	private PdfReader pdfReader;
	//</editor-fold>

	//<editor-fold desc="Constructors">
	public PdfReaderServiceImpl() {
		this.imageList = new ArrayList<>();
	}
	//</editor-fold>

	//<editor-fold desc="Methods">
	@Override
	public boolean open(String filename) {
		try {
			pdfReader = new PdfReader(filename);
		} catch(IOException e) {
			return true;
		} catch(OutOfMemoryError e) {
			return true;
		}
		return false;
	}

	@Override
	public int getPages() {
		return pdfReader.getNumberOfPages();
	}

	@Override
	public String getAuthor() {
		String author = pdfReader.getInfo().get("Author");
		if (author != null) {
			return author;
		}
		return "";
	}

	@Override
	public String getPageText(int page) {
		try {
			return PdfTextExtractor.getTextFromPage(pdfReader, page) + "\n";
		} catch(IOException e) {
			System.err.println("Failed to extract text " + e.getMessage());
			return "";
		} catch (OutOfMemoryError e) {
			System.err.println("Out of memory in text extraction " + e.getMessage());
			return "";
		}
	}

	@Override
	public List<String> getPageImages(int page, ImageRenderedCallback imageRenderer) {
		PdfReaderContentParser parser = new PdfReaderContentParser(pdfReader);
		ImageRenderListener listener = new ImageRenderListener(imageRenderer);
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

	@Override
	public String getBookmarks() {
		List<HashMap<String, Object>> list;
		try {
			list = SimpleBookmark.getBookmark(pdfReader);
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

	@Override
	public void addImage(String image){
		imageList.add(image);
	}
	//</editor-fold>
}

