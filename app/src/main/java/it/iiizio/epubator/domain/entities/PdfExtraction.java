package it.iiizio.epubator.domain.entities;

import java.util.ArrayList;
import java.util.List;

import it.iiizio.epubator.domain.utils.HtmlHelper;
import it.iiizio.epubator.domain.utils.PdfReadHelper;
import it.iiizio.epubator.domain.callbacks.PageBuildEvents;

public class PdfExtraction {

    //<editor-fold desc="Attributes">
	private final String REGEX_ANY_ERROR = ".*\\p{Cntrl}.*";
	private final String REGEX_AN_ERROR = "\\p{Cntrl}+";
    private final List<String> pdfImages;
    private boolean extractionError;
    private final ConversionPreferences preferences;
    private final PageBuildEvents build;
    private final int pages;
    private final int pagesPerFile;
    //</editor-fold>

	//<editor-fold desc="Constructors">
	public PdfExtraction(ConversionPreferences preferences, int pages, int pagesPerFile, PageBuildEvents build) {
        this.preferences = preferences;
        this.build = build;
        this.pages = pages;
        this.pagesPerFile = pagesPerFile;
        pdfImages = new ArrayList<>();
    }
	//</editor-fold>

	//<editor-fold desc="Methods">
	public List<String> getPdfImages() {
		return pdfImages;
	}

	public String buildPage(int pageIndex){
		build.pageAdded(pageIndex);
		StringBuilder pageText = new StringBuilder();

		int endPage = Math.min(pageIndex + pagesPerFile - 1, pages);

		for (int j = pageIndex; j <= endPage; j++) {
			String singlePageText = buildSinglePage(j);
			pageText.append(singlePageText);
		}

		return pageText.toString();
	}

	private String buildSinglePage(int pageIndex) {
		StringBuilder pageText = new StringBuilder();

		// Add anchor
		pageText.append("  <p>\n");
		pageText.append("  <a id=\"page" + pageIndex + "\"/>\n");

		// extract text
		String page = HtmlHelper.stringToHTMLString(PdfReadHelper.getPageText(pageIndex));
		if (page.length() == 0) {
			build.pageFailure(pageIndex);
			hasExtractionError(true);
			if (preferences.addMarkers) {
				pageText.append(pageErrorMarker(pageIndex));
			}
		} else {
			if (page.matches(REGEX_ANY_ERROR)) {
				hasExtractionError(true);
				String marker = preferences.addMarkers ? pageTextErrorMarker(pageIndex) : " ";
				pageText.append(page.replaceAll(REGEX_AN_ERROR, marker));
			} else {
				pageText.append(page);
			}
		}

		if (preferences.includeImages) {
			String pageImages = addImages(pageIndex);
			pageText.append(pageImages);
		}

		// Close page
		pageText.append("\n  </p>\n");

		return pageText.toString();
	}

	private String addImages(int pageIndex){
		StringBuilder pageText = new StringBuilder();

		List<String> imageList = PdfReadHelper.getPageImages(pageIndex);
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

		return pageText.toString();
	}

	/**
	 * Add error marker with the format '<<#NUMBER_OF_PAGE>>'
	 * @param pageIndex
	 * @return
	 */
	private String pageErrorMarker(int pageIndex){
		return "&lt;&lt;#" + pageIndex + "&gt;&gt;";
	}

	/**
	 * Add error marker with the format '<<@NUMBER_OF_PAGE>>'
	 * @param pageIndex
	 * @return
	 */
	private String pageTextErrorMarker(int pageIndex){
		return "&lt;&lt;@" + pageIndex + "&gt;&gt;";
	}

	private void hasExtractionError(boolean hasError){
        extractionError = hasError;
    }

    public boolean hadExtractionError(){
        return extractionError;
    }

	private boolean imageNotAddedYet(String image){
        return !pdfImages.contains(image);
    }

	private void addImage(String image){
        pdfImages.add(image);
    }
	//</editor-fold>
}
