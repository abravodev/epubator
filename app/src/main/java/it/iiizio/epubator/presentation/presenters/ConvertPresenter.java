package it.iiizio.epubator.presentation.presenters;

import it.iiizio.epubator.domain.exceptions.ConversionException;
import it.iiizio.epubator.presentation.callbacks.PageBuild;
import it.iiizio.epubator.presentation.dto.ConversionPreferences;
import it.iiizio.epubator.presentation.dto.ConversionSettings;
import it.iiizio.epubator.presentation.dto.PdfExtraction;

public interface ConvertPresenter {

    void loadPdfFile(String pdfFilename) throws ConversionException;
    void openFile(String tempFilename) throws ConversionException;
    void addMimeType() throws ConversionException;
    void addContainer() throws ConversionException;
    void addToc(int pages, String tocId, String title, boolean tocFromPdf, int pagesPerFile) throws ConversionException;
    void addFrontPage() throws ConversionException;
    void addFrontpageCover(String bookFilename, String coverImageFilename, boolean showLogoOnCover) throws ConversionException;
    PdfExtraction addPages(ConversionPreferences preferences, int pages, int pagesPerFile, PageBuild build) throws ConversionException;
    void addContent(int pages, String id, String title, Iterable<String> images, int pagesPerFile) throws ConversionException;
    void closeFile(String tempFilename) throws ConversionException;
    void saveEpub(ConversionSettings settings);
    boolean deleteTemporalFile(ConversionSettings settings);
}
