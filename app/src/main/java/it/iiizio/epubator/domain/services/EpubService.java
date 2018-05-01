package it.iiizio.epubator.domain.services;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

import it.iiizio.epubator.domain.entities.Book;

public interface EpubService {

	Book getBook(ZipFile epubFile) throws IOException;

	void saveHtmlPage(File htmlFile, String htmlText) throws IOException;

	void saveImages(ZipFile epubFile, String htmlPage, File imageDirectory);

	String getHtmlPage(ZipFile epubFile, String htmlFile) throws IOException;

}
