package it.iiizio.epubator.domain.utils;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.List;

public interface PdfXmlParser {

	boolean hasGoToAction(Element chapterElement);

	String getChapterTitle(Element chapterElement);

	int getChapterPage(Element chapterElement);

	String getAnchor(Element chapterElement);

	NodeList getNavigationPoints(String toc);

	NodeList getDocumentTitles(String bookmarks);

	List<String> extractImages(String htmlPage);

}
