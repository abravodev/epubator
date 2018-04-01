package it.iiizio.epubator.presenters;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipFile;

import it.iiizio.epubator.model.entities.Book;

public interface VerifyPresenter {

    Book getBook(ZipFile epubFile) throws IOException;

    List<String> getPages(ZipFile epubFile);

    String getHtmlPage(ZipFile epubFile, String htmlFile) throws IOException;

    void saveImages(ZipFile epubFile, String htmlPage, File imageDirectory);

    void saveHtmlPage(File htmlFile, String htmlText) throws IOException;
}
