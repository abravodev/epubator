package it.iiizio.epubator.domain.services;

import java.io.IOException;
import java.util.zip.ZipFile;

import it.iiizio.epubator.domain.entities.EBook;

public interface EpubService {

	EBook getBook(String filename) throws IOException;

	void saveHtmlPage(String htmlFile, String htmlText) throws IOException;

	void saveImages(ZipFile epubFile, String htmlPage, String imageDirectory);

	String getHtmlPage(ZipFile epubFile, String htmlFile) throws IOException;

}
