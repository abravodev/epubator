package it.iiizio.epubator.domain.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class PdfXmlParserImpl implements PdfXmlParser {

	private static final int ANCHOR_ELEMENT_INDEX = 3;
	private final XMLParser parser;

	public PdfXmlParserImpl(){
		this.parser = new XMLParser();
	}

	@Override
	public boolean hasGoToAction(Element chapterElement){
		String action = parser.getValue(chapterElement, "Action");
		return action.equals("GoTo");
	}

	@Override
	public String getChapterTitle(Element chapterElement){
		return parser.getElementValue(chapterElement).trim();
	}

	@Override
	public int getChapterPage(Element chapterElement){
		String chapterPage = parser.getValue(chapterElement, "Page").split(" ")[0];
		return Integer.parseInt(chapterPage);
	}

	@Override
	public String getAnchor(Element chapterElement){
		Element anchorElement = (Element) chapterElement.getChildNodes().item(ANCHOR_ELEMENT_INDEX);
		return parser.getValue(anchorElement, "src");
	}

	@Override
	public NodeList getNavigationPoints(String toc){
		return getTagElementFromDocument(toc, "navPoint");
	}

	@Override
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
