package it.iiizio.epubator.domain.services;

import java.util.List;

import it.iiizio.epubator.domain.callbacks.ImageRenderedCallback;

public interface PdfReaderService {

	boolean open(String filename);

	int getPages();

	String getAuthor();

	String getPageText(int page);

	List<String> getPageImages(int page, ImageRenderedCallback imageRenderer);

	String getBookmarks();

	void addImage(String image);

}
