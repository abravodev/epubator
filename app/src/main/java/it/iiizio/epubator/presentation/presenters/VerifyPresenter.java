package it.iiizio.epubator.presentation.presenters;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

import it.iiizio.epubator.domain.entities.Book;

public interface VerifyPresenter {

    Book getBook(ZipFile epubFile) throws IOException;

    String getHtmlPage(ZipFile epubFile, String htmlFile) throws IOException;

    void saveImages(ZipFile epubFile, String htmlPage, File imageDirectory);

    void saveHtmlPage(File htmlFile, String htmlText) throws IOException;
}
