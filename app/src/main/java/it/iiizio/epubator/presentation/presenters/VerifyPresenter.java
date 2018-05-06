package it.iiizio.epubator.presentation.presenters;

import java.io.IOException;
import java.util.zip.ZipFile;

import it.iiizio.epubator.domain.entities.Book;

public interface VerifyPresenter {

    Book getBook(ZipFile epubFile) throws IOException;

    String getHtmlPage(ZipFile epubFile, String htmlFile) throws IOException;

    String saveHtmlPage(ZipFile epubFile, String htmlText) throws IOException;

    boolean showImages();

    void removeFilesFromTemporalDirectory();
}
