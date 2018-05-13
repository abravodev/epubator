package it.iizio.epubator.domain.services;

import org.junit.jupiter.api.Test;

import it.iiizio.epubator.domain.services.ConversionService;
import it.iiizio.epubator.domain.services.ConversionServiceImpl;
import it.iiizio.epubator.infrastructure.providers.StorageProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConversionServiceTest {

	@Test
	public void getOutputDirectory_userPrefersToSaveOnDownloadDirectory_returnsDownloadDirectory(){
	    // Arrange
		String downloadDirectory = "path/to/downloads";
		StorageProvider storageProvider = mock(StorageProvider.class);
		when(storageProvider.getDownloadDirectory()).thenReturn(downloadDirectory);
		ConversionService service = makeService(storageProvider);

	    // Act
		String outputDirectory = service.getOutputDirectory("anyPdfFilename", true);

	    // Assert
		assertEquals(downloadDirectory, outputDirectory);
	}

	@Test
	public void getOutputDirectory_directoryOfPdfFileIsNotWritable_returnsDownloadDirectory(){
	    // Arrange
		String downloadDirectory = "path/to/downloads";
		String folderFile = "path/to/folder/";
		String filename = "path/to/folder/filename.pdf";
		StorageProvider storageProvider = mock(StorageProvider.class);
		when(storageProvider.getDownloadDirectory()).thenReturn(downloadDirectory);
		when(storageProvider.folderIsWritable(folderFile)).thenReturn(false);
		ConversionService service = makeService(storageProvider);

	    // Act
		String outputDirectory = service.getOutputDirectory(filename, false);

	    // Assert
		assertEquals(downloadDirectory, outputDirectory);
	}

	@Test
	public void getOutputDirectory_directoryOfPdfFileIsWritable_returnsDirectoryOfPdfFile(){
		// Arrange
		String folderFile = "path/to/folder/";
		String filename = "path/to/folder/filename.pdf";
		StorageProvider storageProvider = mock(StorageProvider.class);
		when(storageProvider.folderIsWritable(folderFile)).thenReturn(true);
		ConversionService service = makeService(storageProvider);

		// Act
		String outputDirectory = service.getOutputDirectory(filename, false);

		// Assert
		assertEquals(folderFile, outputDirectory);
	}

	private ConversionService makeService(StorageProvider storageProvider){
		storageProvider = storageProvider != null ? storageProvider : mock(StorageProvider.class);
		return new ConversionServiceImpl(storageProvider);
	}
}
