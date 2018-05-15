package it.iizio.epubator.presentation.presenters;

import org.junit.jupiter.api.Test;

import it.iiizio.epubator.domain.entities.ConversionPreferences;
import it.iiizio.epubator.domain.entities.ConversionSettings;
import it.iiizio.epubator.domain.services.ConversionService;
import it.iiizio.epubator.domain.services.ConversionServiceImpl;
import it.iiizio.epubator.infrastructure.providers.PreferenceProvider;
import it.iiizio.epubator.infrastructure.providers.StorageProvider;
import it.iiizio.epubator.presentation.presenters.ConvertPresenter;
import it.iiizio.epubator.presentation.presenters.ConvertPresenterImpl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConvertPresenterTest {

	@Test
	public void deleteTemporalFile_fileDoesntExist_returnsFalse(){
	    // Arrange
		StorageProvider storageProvider = mock(StorageProvider.class);
		String filenameThatDoesntExist = "path/to/filename.pdf";
		ConversionSettings settings = new ConversionSettings(mock(ConversionPreferences.class),
			"anyOutputDirectory", "anyTemporalPath", filenameThatDoesntExist, "anyCoverFile");
		when(storageProvider.exists(settings.oldFilename)).thenReturn(false);
		ConvertPresenter presenter = makePresenter(storageProvider);

	    // Act
		boolean exists = presenter.deleteTemporalFile(settings);

	    // Assert
		assertFalse(exists);
	}

	@Test
	public void deleteTemporalFile_fileExistsButCannotRenameIt_returnsTrue(){
		StorageProvider storageProvider = mock(StorageProvider.class);
		String filenameThatDoesntExist = "path/to/filename.pdf";
		ConversionSettings settings = new ConversionSettings(mock(ConversionPreferences.class),
			"anyOutputDirectory", "anyTemporalPath", filenameThatDoesntExist, "anyCoverFile");
		when(storageProvider.exists(settings.oldFilename)).thenReturn(true);
		when(storageProvider.rename(settings.oldFilename, settings.epubFilename)).thenReturn(false);
		ConvertPresenter presenter = makePresenter(storageProvider);

		// Act
		boolean exists = presenter.deleteTemporalFile(settings);

		// Assert
		assertFalse(exists);
	}

	@Test
	public void deleteTemporalFile_fileExistsAndCanRenameIt_returnsTrue(){
		StorageProvider storageProvider = mock(StorageProvider.class);
		String filenameThatDoesntExist = "path/to/filename.pdf";
		ConversionSettings settings = new ConversionSettings(mock(ConversionPreferences.class),
			"anyOutputDirectory", "anyTemporalPath", filenameThatDoesntExist, "anyCoverFile");
		when(storageProvider.exists(settings.oldFilename)).thenReturn(true);
		when(storageProvider.rename(settings.oldFilename, settings.epubFilename)).thenReturn(true);
		ConvertPresenter presenter = makePresenter(storageProvider);

		// Act
		boolean exists = presenter.deleteTemporalFile(settings);

		// Assert
		assertTrue(exists);
	}

	@Test
	public void deleteTemporalFile_temporalFileIsDeleted(){
	    // Arrange
		StorageProvider storageProvider = mock(StorageProvider.class);
		String filenameThatDoesntExist = "path/to/filename.pdf";
		ConversionSettings settings = new ConversionSettings(mock(ConversionPreferences.class),
			"anyOutputDirectory", "anyTemporalPath", filenameThatDoesntExist, "anyCoverFile");
		when(storageProvider.exists(settings.oldFilename)).thenReturn(true);
		ConvertPresenter presenter = makePresenter(storageProvider);

	    // Act
		presenter.deleteTemporalFile(settings);

	    // Assert
		verify(storageProvider, times(1)).remove(settings.tempFilename);
	}

	@Test
	public void deleteTemporalFile_fileExists_fileItsRenamed(){
		StorageProvider storageProvider = mock(StorageProvider.class);
		String filenameThatDoesntExist = "path/to/filename.pdf";
		ConversionSettings settings = new ConversionSettings(mock(ConversionPreferences.class),
			"anyOutputDirectory", "anyTemporalPath", filenameThatDoesntExist, "anyCoverFile");
		when(storageProvider.exists(settings.oldFilename)).thenReturn(true);
		ConvertPresenter presenter = makePresenter(storageProvider);

		// Act
		presenter.deleteTemporalFile(settings);

		// Assert
		verify(storageProvider, times(1)).rename(settings.oldFilename, settings.epubFilename);
	}

	private ConvertPresenter makePresenter(StorageProvider storageProvider){
		return makePresenter(null, storageProvider, new ConversionServiceImpl(storageProvider));
	}

	private ConvertPresenter makePresenter(PreferenceProvider preferenceProvider, StorageProvider storageProvider, ConversionService conversionService){
		preferenceProvider = preferenceProvider != null ? preferenceProvider : mock(PreferenceProvider.class);
		storageProvider = storageProvider!=null ? storageProvider : mock(StorageProvider.class);
		conversionService = conversionService!=null ? conversionService : mock(ConversionService.class);
		return new ConvertPresenterImpl(preferenceProvider, conversionService, storageProvider);
	}
}
