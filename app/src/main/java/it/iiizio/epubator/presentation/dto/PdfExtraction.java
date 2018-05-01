package it.iiizio.epubator.presentation.dto;

import java.util.ArrayList;
import java.util.List;

import it.iiizio.epubator.domain.utils.HtmlHelper;
import it.iiizio.epubator.domain.utils.PdfReadHelper;
import it.iiizio.epubator.presentation.callbacks.PageBuildEvents;

public class PdfExtraction {

    private final List<String> pdfImages;
    private boolean extractionError;
    private final ConversionPreferences preferences;
    private final PageBuildEvents build;
    private final int pages;
    private final int pagesPerFile;

    public PdfExtraction(ConversionPreferences preferences, int pages, int pagesPerFile, PageBuildEvents build) {
        this.preferences = preferences;
        this.build = build;
        this.pages = pages;
        this.pagesPerFile = pagesPerFile;
        pdfImages = new ArrayList<>();
    }

    public void hasExtractionError(boolean hasError){
        extractionError = hasError;
    }

    public boolean hadExtractionError(){
        return extractionError;
    }

    public boolean imageNotAddedYet(String image){
        return !pdfImages.contains(image);
    }

    public void addImage(String image){
        pdfImages.add(image);
    }

    public List<String> getPdfImages() {
        return pdfImages;
    }

    public String buildPage(int pageIndex){
        build.pageAdded(pageIndex);
        StringBuilder pageText = new StringBuilder();

        int endPage = Math.min(pageIndex + pagesPerFile - 1, pages);

        for (int j = pageIndex; j <= endPage; j++) {
            // Add anchor
            pageText.append("  <p>\n");
            pageText.append("  <a id=\"page" + j + "\"/>\n");

            // extract text
            String page = HtmlHelper.stringToHTMLString(PdfReadHelper.getPageText(j));
            if (page.length() == 0) {
                build.pageFailure(j);
                hasExtractionError(true);
                if (preferences.addMarkers) {
                    pageText.append("&lt;&lt;#" + j + "&gt;&gt;");
                }
            } else {
                if (page.matches(".*\\p{Cntrl}.*")) {
                    hasExtractionError(true);
                    if (preferences.addMarkers) {
                        pageText.append(page.replaceAll("\\p{Cntrl}+", "&lt;&lt;@" + j + "&gt;&gt;"));
                    } else {
                        pageText.append(page.replaceAll("\\p{Cntrl}+", " "));
                    }
                } else {
                    pageText.append(page);
                }
            }

            if (preferences.includeImages) {
                List<String> imageList = PdfReadHelper.getPageImages(j);
                for (String imageName: imageList){
                    String imageTag = "\n<img alt=\"" + imageName + "\" src=\"" + imageName + "\" /><br/>";

                    if (imageNotAddedYet(imageName)) {
                        addImage(imageName);
                        build.imageAdded(imageName);
                        pageText.append(imageTag);
                    } else if (preferences.repeatedImages) {
                        pageText.append(imageTag);
                    }
                }
            }
            // Close page
            pageText.append("\n  </p>\n");
        }

        return pageText.toString();
    }
}
