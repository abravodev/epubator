package it.iiizio.epubator.domain.utils;

import com.google.common.base.Strings;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

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
		if(Strings.isNullOrEmpty(chapterPage)){
			return -1;
		}
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

	@Override
	public List<String> extractImages(String htmlPage) {
		List<String> images = new ArrayList<>();
		try {
			XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = parserFactory.newPullParser();
			parser.setInput(new StringReader(htmlPage.replaceAll("&nbsp;", "")));
			int eventType = parser.getEventType();

			while (eventType != XmlPullParser.END_DOCUMENT) {
				if(eventType == XmlPullParser.START_TAG && "img".equals(parser.getName())) {
					String imageName = parser.getAttributeValue(null, "src");
					images.add(imageName);
				}
				eventType = parser.next();
			}
		} catch (XmlPullParserException e) {
			System.err.println("XmlPullParserException in image preview");
		} catch (IOException e) {
			System.err.println("IOException in image preview");
		}

		return images;
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
