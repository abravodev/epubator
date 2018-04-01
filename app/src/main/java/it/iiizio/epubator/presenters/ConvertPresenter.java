package it.iiizio.epubator.presenters;

public interface ConvertPresenter {

    boolean addMimeType();
    boolean addContainer();
    boolean addToc(int pages, String tocId, String title, boolean tocFromPdf, int pagesPerFile);
    boolean addFrontPage();
    boolean addFrontpageCover(String bookFilename, String coverImageFilename, boolean showLogoOnCover);
    boolean addContent(int pages, String id, Iterable<String> images, String title, int pagesPerFile);
    boolean addPage(int page, String text);

}
