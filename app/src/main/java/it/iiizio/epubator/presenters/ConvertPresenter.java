package it.iiizio.epubator.presenters;

import it.iiizio.epubator.model.exceptions.ConversionException;
import it.iiizio.epubator.views.dto.ConversionSettings;

public interface ConvertPresenter {

    void loadPdfFile(String pdfFilename) throws ConversionException;
    void openFile(String tempFilename) throws ConversionException;
    void addMimeType() throws ConversionException;
    void addContainer() throws ConversionException;
    void addToc(int pages, String tocId, String title, boolean tocFromPdf, int pagesPerFile) throws ConversionException;
    void addFrontPage() throws ConversionException;
    void addFrontpageCover(String bookFilename, String coverImageFilename, boolean showLogoOnCover) throws ConversionException;
    void addPage(int page, String text) throws ConversionException;
    void addContent(int pages, String id, String title, Iterable<String> images, int pagesPerFile) throws ConversionException;
    void closeFile(String tempFilename) throws ConversionException;
    void saveEpub(ConversionSettings settings);
    boolean deleteTemporalFile(ConversionSettings settings);
}
