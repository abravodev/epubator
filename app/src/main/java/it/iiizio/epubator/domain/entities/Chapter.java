package it.iiizio.epubator.domain.entities;

import org.w3c.dom.Element;

import it.iiizio.epubator.domain.utils.XMLParser;

public class Chapter {

	private final String title;
	private final int pageIndex;

	public Chapter(String title, int pageIndex) {
		this.title = title;
		this.pageIndex = pageIndex;
	}

	public static Chapter make(XMLParser parser, Element chapterElement){
		String action = parser.getValue(chapterElement, "Action");
		if(!action.equals("GoTo")){
			return null;
		}

		String chapterTitle = parser.getElementValue(chapterElement).trim();
		String chapterPage = parser.getValue(chapterElement, "Page").split(" ")[0];
		int pageIndex = Integer.parseInt(chapterPage);
		return new Chapter(chapterTitle, pageIndex);
	}

	public String getTitle() {
		return title;
	}

	public int getPageIndex() {
		return pageIndex;
	}

}
