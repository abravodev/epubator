package it.iizio.epubator.domain.entities;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import it.iiizio.epubator.domain.entities.Chapter;
import it.iiizio.epubator.domain.utils.PdfXmlParser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChapterTest {

	@Test
	public void make_chapterWithoutGotoAction_returnsNull(){
	    // Arrange
		Element chapterElement = mock(Element.class);
		PdfXmlParser parser = mock(PdfXmlParser.class);
		when(parser.hasGoToAction(chapterElement)).thenReturn(false);

	    // Act
		Chapter chapter = Chapter.make(parser, chapterElement);

	    // Assert
		assertNull(chapter);
	}

	@Test
	public void make_chapterWithGotoActionButWithoutPageNumber_returnsNull(){
	    // Arrange
		Element chapterElement = mock(Element.class);
		PdfXmlParser parser = mock(PdfXmlParser.class);
		when(parser.hasGoToAction(chapterElement)).thenReturn(true);
		when(parser.getChapterPage(chapterElement)).thenReturn(-1);

	    // Act
		Chapter chapter = Chapter.make(parser, chapterElement);

	    // Assert
		assertNull(chapter);
	}

	@Test
	public void make_chapterWithGotoActionAndPageNumber_returnsChapter(){
	    // Arrange
		Element chapterElement = mock(Element.class);
		String chapterTitle = "chaptertitle";
		int chapterPage = 1;
		PdfXmlParser parser = mock(PdfXmlParser.class);
		when(parser.hasGoToAction(chapterElement)).thenReturn(true);
		when(parser.getChapterTitle(chapterElement)).thenReturn(chapterTitle);
		when(parser.getChapterPage(chapterElement)).thenReturn(chapterPage);

	    // Act
		Chapter chapter = Chapter.make(parser, chapterElement);

	    // Assert
		assertEquals(chapterTitle, chapter.getTitle());
		assertEquals(chapterPage, chapter.getPageIndex());
	}
}
