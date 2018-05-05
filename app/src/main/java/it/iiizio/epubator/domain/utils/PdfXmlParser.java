package it.iiizio.epubator.domain.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class PdfXmlParser {

	private static final int ANCHOR_ELEMENT_INDEX = 3;
	private final XMLParser parser;

	public PdfXmlParser(){
		this.parser = new XMLParser();
	}

	public boolean hasGoToAction(Element chapterElement){
		String action = parser.getValue(chapterElement, "Action");
		return action.equals("GoTo");
	}

	public String getChapterTitle(Element chapterElement){
		return parser.getElementValue(chapterElement).trim();
	}

	public int getChapterPage(Element chapterElement){
		String chapterPage = parser.getValue(chapterElement, "Page").split(" ")[0];
		return Integer.parseInt(chapterPage);
	}

	public String getAnchor(Element chapterElement){
		Element anchorElement = (Element) chapterElement.getChildNodes().item(ANCHOR_ELEMENT_INDEX);
		return parser.getValue(anchorElement, "src");
	}

	public NodeList getNavigationPoints(String toc){
		return getTagElementFromDocument(toc, "navPoint");
	}

	public NodeList getDocumentTitles(String bookmarks){
		return getTagElementFromDocument(bookmarks, "Title");
	}

	private NodeList getTagElementFromDocument(String domElement, String tagElement){
		Document doc = parser.getDomElement(domElement);
		if (doc == null) {
			return null;
		}

		doc.normalize();
		return doc.getElementsByTagName(tagElement);
	}
}
