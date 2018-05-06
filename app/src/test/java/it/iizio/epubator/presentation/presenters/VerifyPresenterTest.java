package it.iizio.epubator.presentation.presenters;

import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipFile;

import it.iiizio.epubator.domain.entities.Book;
import it.iiizio.epubator.domain.services.EpubService;
import it.iiizio.epubator.infrastructure.providers.PreferenceProvider;
import it.iiizio.epubator.infrastructure.providers.StorageProvider;
import it.iiizio.epubator.presentation.presenters.VerifyPresenter;
import it.iiizio.epubator.presentation.presenters.VerifyPresenterImpl;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VerifyPresenterTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void getBook_bookWithoutPages_throwsException() throws IOException {
	    // Arrange
		EpubService epubService = mock(EpubService.class);
		VerifyPresenter presenter = makePresenter(epubService, null, null);
		ZipFile anyZipFile = mock(ZipFile.class);
		Book bookWithoutPages = new Book(new ArrayList<String>());
		when(epubService.getBook(anyZipFile)).thenReturn(bookWithoutPages);

	    // Act/Assert
		thrown.expect(IOException.class);
		thrown.expectMessage(containsString("no pages"));
		presenter.getBook(anyZipFile);
	}

	@Test
	public void getBook_bookWithPages_returnsBook() throws IOException {
		// Arrange
		EpubService epubService = mock(EpubService.class);
		VerifyPresenter presenter = makePresenter(epubService, null, null);
		ZipFile anyZipFile = mock(ZipFile.class);
		List<String> bookPages = new ArrayList<>(Arrays.asList("Page1"));
		Book bookWithPages = new Book(bookPages);
		when(epubService.getBook(anyZipFile)).thenReturn(bookWithPages);

		// Act
		Book book = presenter.getBook(anyZipFile);

		// Assert
		Assertions.assertSame(bookWithPages, book);
	}

	private VerifyPresenter makePresenter(EpubService epubService, PreferenceProvider preferenceProvider, StorageProvider storageProvider) {
		epubService = epubService == null ? mock(EpubService.class) : epubService;
		preferenceProvider = preferenceProvider == null ? mock(PreferenceProvider.class) : preferenceProvider;
		storageProvider = storageProvider == null ? mock(StorageProvider.class) : storageProvider;

		return new VerifyPresenterImpl(epubService, preferenceProvider, storageProvider);
	}
}
