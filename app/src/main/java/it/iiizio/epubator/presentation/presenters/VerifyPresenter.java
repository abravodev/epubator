package it.iiizio.epubator.presentation.presenters;

import java.io.IOException;
import java.util.zip.ZipFile;

import it.iiizio.epubator.domain.entities.EBook;

public interface VerifyPresenter {

    EBook getBook(String filename) throws IOException;

    String getHtmlPage(ZipFile epubFile, String htmlFile) throws IOException;

    String saveHtmlPage(ZipFile epubFile, String htmlText) throws IOException;

    boolean showImages();

    void removeFilesFromTemporalDirectory();

    void closeBook(EBook book) throws IOException;
}
