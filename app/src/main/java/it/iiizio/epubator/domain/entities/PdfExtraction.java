package it.iiizio.epubator.domain.entities;

import java.util.ArrayList;
import java.util.List;

import it.iiizio.epubator.domain.callbacks.ImageRenderedCallback;
import it.iiizio.epubator.domain.callbacks.PageBuildEvents;
import it.iiizio.epubator.domain.constants.ZipFileConstants;
import it.iiizio.epubator.domain.services.PdfReaderService;
import it.iiizio.epubator.domain.services.ZipWriterService;
import it.iiizio.epubator.domain.utils.HtmlHelper;

public class PdfExtraction {

    //<editor-fold desc="Attributes">
	private static final String REGEX_ANY_ERROR = ".*\\p{Cntrl}.*";
	private static final String REGEX_AN_ERROR = "\\p{Cntrl}+";
	private final PdfReaderService pdfReader;
	private final ZipWriterService zipWriter;
	private final ConversionPreferences preferences;
	private final PageBuildEvents buildEvents;
	private final List<String> pdfImages;
	private boolean extractionError;
	//</editor-fold>

	//<editor-fold desc="Constructors">
	public PdfExtraction(ConversionPreferences preferences, PageBuildEvents buildEvents,
			 PdfReaderService pdfReader, ZipWriterService zipWriter) {
        this.preferences = preferences;
        this.buildEvents = buildEvents;
		this.pdfReader = pdfReader;
		this.zipWriter = zipWriter;
		this.pdfImages = new ArrayList<>();
    }
	//</editor-fold>

	//<editor-fold desc="Methods">
	public List<String> getPdfImages() {
		return pdfImages;
	}

	public String buildPage(int pageIndex){
		StringBuilder pageText = new StringBuilder();

		int endPage = Math.min(pageIndex + preferences.pagesPerFile - 1, getPages());
		for (int j = pageIndex; j <= endPage; j++) {
			String singlePageText = buildSinglePage(j);
			pageText.append(singlePageText);
		}

		return pageText.toString();
	}

	public int getPages(){
		return pdfReader.getPages();
	}

	public boolean hadExtractionError(){
		return extractionError;
	}

	private String buildSinglePage(int pageIndex) {
		StringBuilder pageText = new StringBuilder();

		// Add anchor
		pageText.append("  <p>\n");
		pageText.append("  <a id=\"page" + pageIndex + "\"/>\n");

		// extract text
		String page = HtmlHelper.stringToHTMLString(pdfReader.getPageText(pageIndex));
		if (page.length() == 0) {
			buildEvents.pageFailure(pageIndex);
			errorHappened();
			if (preferences.addMarkers) {
				pageText.append(pageErrorMarker(pageIndex));
			}
		} else {
			if (page.matches(REGEX_ANY_ERROR)) {
				errorHappened();
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

		ImageRenderedCallback imageRenderedCallback = new ImageRenderedCallback() {
			@Override
			public void imageRendered(String imageName, byte[] image) {
				if (!zipWriter.addImage(ZipFileConstants.image(imageName), image)) {
					pdfReader.addImage(imageName);
				}
			}
		};

		List<String> imageList = pdfReader.getPageImages(pageIndex, imageRenderedCallback);
		for (String imageName: imageList){
			String imageTag = "\n<img alt=\"" + imageName + "\" src=\"" + imageName + "\" /><br/>";

			if (imageNotAddedYet(imageName)) {
				addImage(imageName);
				buildEvents.imageAdded(imageName);
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

	private void errorHappened(){
        extractionError = true;
    }

	private boolean imageNotAddedYet(String image){
        return !pdfImages.contains(image);
    }

	private void addImage(String image){
        pdfImages.add(image);
    }
	//</editor-fold>
}
