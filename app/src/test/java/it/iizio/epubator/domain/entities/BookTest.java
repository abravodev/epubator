package it.iizio.epubator.domain.entities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.Arrays;

import it.iiizio.epubator.domain.constants.ZipFileConstants;
import it.iiizio.epubator.domain.entities.Book;
import it.iiizio.epubator.domain.exceptions.NotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BookTest {

	@Test
	public void addChapter_addAnyChapter_chapterIsAdded(){
	    // Arrange
		Book anyBook = makeBook();
		String anyChapter = "chapter";
		int chaptersBefore = anyBook.getChapters().length;

	    // Act
		anyBook.addChapter(anyChapter);
		int chaptersAfter = anyBook.getChapters().length;

	    // Assert
		assertEquals(1, chaptersAfter - chaptersBefore);
	}

	@Test
	public void addAnchor_addAnyAnchor_anchorIsAdded(){
	    // Arrange
		Book anyBook = makeBook();
		String anchorName = "anchor";
		String anyAnchor = String.format("anchor#%s", anchorName);

	    // Act
		anyBook.addAnchor(anyAnchor);
		String anchor = anyBook.getAnchor(0);

		// Assert
		assertEquals(anchorName, anchor);
	}

	@Test
	public void getPagesCount_emptyBook_returnsZero(){
	    // Arrange
		Book emptyBook = makeEmptyBook();

	    // Act
		int pages = emptyBook.getPagesCount();

	    // Assert
		assertEquals(0, pages);
	}

	@Test
	public void getPagesCount_bookWithOnePage_returnsOne(){
	    // Arrange
		Book bookWithOnePage = makeBookWithOnePage();

	    // Act
		int pages = bookWithOnePage.getPagesCount();

	    // Assert
		assertEquals(1, pages);
	}

	@Test
	public void getPage_firstPageFromBookWithPages_returnsTheFirstPage() throws NotFoundException {
	    // Arrange
		String firstPage = "Page1";
		Book book = makeBook(firstPage);
		int firstPageIndex = 1;

	    // Act
		String page = book.getPage(firstPageIndex);

	    // Assert
		assertEquals(firstPage, page);
	}

	@Test
	public void getPage_secondPageFromBookWithPages_returnsTheSecondPage() throws NotFoundException {
		// Arrange
		String firstPage = "Page1";
		String secondPage = "Page2";
		Book book = makeBook(firstPage, secondPage);
		int firstPageIndex = 2;

		// Act
		String page = book.getPage(firstPageIndex);

		// Assert
		assertEquals(secondPage, page);
	}

	@Test
	public void getPage_anyPageFromAnEmptyBook_throwsException() throws NotFoundException {
	    // Arrange
		Book emptyBook = makeEmptyBook();
		int anyPageIndex = 1;

	    // Act
		Executable getPage = () -> emptyBook.getPage(anyPageIndex);

		// Assert
		assertThrows(NotFoundException.class, getPage);
	}

	@Test
	public void getPage_secondPageFromABookWithOnePage_throwsException() throws NotFoundException {
	    // Arrange
		Book bookWithOnePage = makeBookWithOnePage();
		int pageIndex = 2;

	    // Act
		Executable getPage = () -> bookWithOnePage.getPage(pageIndex);

		// Assert
		assertThrows(NotFoundException.class, getPage);
	}

	@Test
	public void getPageIndex_anchorForTheFirstPage_returnsFirstPageIndex(){
	    // Arrange
		String firstPage = "Page1";
		Book bookWithOnePage = makeBook(firstPage);
		String firstPageAnchor = String.format("%s#anchor", firstPage);
		bookWithOnePage.addAnchor(firstPageAnchor);
		int anchorIndex = 0;

	    // Act
		int pageIndex = bookWithOnePage.getPageIndex(anchorIndex);

	    // Assert
		assertEquals(1, pageIndex);
	}

	@Test
	public void getAnchorFromPageName_firstPageIndex_returnsFirstPageName() throws NotFoundException {
	    // Arrange
		String firstPage = "page1";
		int pageIndex = 1;
		String firstPageFilename = ZipFileConstants.page(1);
		Book anyBook = makeBook(firstPageFilename);

	    // Act
		String anchorName = anyBook.getAnchorFromPageName(pageIndex);

	    // Assert
		assertEquals(firstPage, anchorName);
	}

	@Test
	public void hasPreviousPage_currentPageIsOne_returnsFalse(){
	    // Arrange
		Book anyBook = makeBook();
		int currentPageIndex = 1;

	    // Act
		boolean hasPreviousPage = anyBook.hasPreviousPage(currentPageIndex);

	    // Assert
		assertFalse(hasPreviousPage);
	}

	@Test
	public void hasPreviousPage_currentPageIsTwo_returnsTrue(){
	    // Arrange
		Book anyBook = makeBook();
		int currentPageIndex = 2;

	    // Act
		boolean hasPreviousPage = anyBook.hasPreviousPage(currentPageIndex);

	    // Assert
		assertTrue(hasPreviousPage);
	}

	@Test
	public void hasNextPage_currentPageIsOneFromABookOfOnePage_returnsFalse(){
	    // Arrange
		Book bookWithOnePage = makeBookWithOnePage();
		int currentPageIndex = 1;

	    // Act
		boolean hasNextPage = bookWithOnePage.hasNextPage(currentPageIndex);

	    // Assert
		assertFalse(hasNextPage);
	}

	@Test
	public void hasNextPage_currentPageIsOneFromABookOfTwoPages_returnsTrue(){
	    // Arrange
		Book bookWithTwoPages = makeBookWithTwoPages();
		int currentPageIndex = 1;

	    // Act
		boolean hasNextPage = bookWithTwoPages.hasNextPage(currentPageIndex);

	    // Assert
		assertTrue(hasNextPage);
	}

	@Test
	public void hasNextPage_currentPageIsTwoFromABookOfTwoPages_returnsFalse(){
	    // Arrange
		Book bookWithTwoPages = makeBookWithTwoPages();
		int currentPageIndex = 2;

	    // Act
		boolean hasNextPage = bookWithTwoPages.hasNextPage(currentPageIndex);

	    // Assert
		assertFalse(hasNextPage);
	}

	@Test
	public void isValidPage_pageIndexIsZero_returnsFalse(){
	    // Arrange
		Book anyBook = makeBook();
		int pageIndex = 0;

	    // Act
		boolean isValid = anyBook.isValidPage(pageIndex);

	    // Assert
		assertEquals(false, isValid);
	}

	@Test
	public void isValidPage_pageIndexIsOneFromBookWithNoPages_returnsFalse(){
	    // Arrange
		Book emptyBook = makeEmptyBook();
		int pageIndex = 1;

	    // Act
		boolean isValid = emptyBook.isValidPage(pageIndex);

	    // Assert
		assertEquals(false, isValid);
	}

	@Test
	public void isValidPage_pageIndexIsOneFromBookWithOnePage_returnsTrue(){
		// Arrange
		Book bookWithOnePage = makeBookWithOnePage();
		int pageIndex = 1;

		// Act
		boolean isValid = bookWithOnePage.isValidPage(pageIndex);

		// Assert
		assertEquals(true, isValid);
	}

	@Test
	public void isValidPage_pageIndexIsTwoFromBookWithOnePage_returnsFalse(){
	    // Arrange
		Book bookWithOnePage = makeBookWithOnePage();
		int pageIndex = 2;

	    // Act
		boolean isValid = bookWithOnePage.isValidPage(pageIndex);

	    // Assert
		assertEquals(false, isValid);
	}

	private Book makeEmptyBook(){
		return makeBook();
	}

	private Book makeBookWithOnePage(){
		return makeBook("Page1");
	}

	private Book makeBookWithTwoPages(){
		return makeBook("Page1", "Page2");
	}

	private Book makeBook(String... pages){
		return new Book(Arrays.asList(pages));
	}
}
