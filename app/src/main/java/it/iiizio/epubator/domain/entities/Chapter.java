package it.iiizio.epubator.domain.entities;

import org.w3c.dom.Element;

import it.iiizio.epubator.domain.utils.PdfXmlParser;

public class Chapter {

	private final String title;
	private final int pageIndex;

	public Chapter(String title, int pageIndex) {
		this.title = title;
		this.pageIndex = pageIndex;
	}

	public static Chapter make(PdfXmlParser parser, Element chapterElement){
		if(!parser.hasGoToAction(chapterElement)){
			return null;
		}

		String chapterTitle = parser.getChapterTitle(chapterElement);
		int chapterPage = parser.getChapterPage(chapterElement);
		if(chapterPage<0){
			return null;
		}
		return new Chapter(chapterTitle, chapterPage);
	}

	public String getTitle() {
		return title;
	}

	public int getPageIndex() {
		return pageIndex;
	}

}
