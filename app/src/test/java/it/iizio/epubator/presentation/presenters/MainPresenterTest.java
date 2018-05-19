package it.iizio.epubator.presentation.presenters;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import it.iiizio.epubator.domain.constants.PreferencesKeys;
import it.iiizio.epubator.infrastructure.providers.PreferenceProvider;
import it.iiizio.epubator.infrastructure.providers.StorageProvider;
import it.iiizio.epubator.presentation.presenters.MainPresenter;
import it.iiizio.epubator.presentation.presenters.MainPresenterImpl;
import it.iizio.epubator.infrastructure.providers.PreferenceProviderFake;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MainPresenterTest {

	@Test
	public void getCoverFileWithTheSameName_nullFilename_returnsEmpty(){
		// Arrange
		MainPresenter presenter = makePresenter();
		String aNullFilename = null;

		// Act
		String coverFile = presenter.getCoverFileWithTheSameName(aNullFilename);

		// Assert
		assertEquals("", coverFile);
	}

	@Test
	public void getCoverFileWithTheSameName_fileWithNoExtension_returnsEmpty(){
		// Arrange
		MainPresenter presenter = makePresenter();
		String anyFilenameWithNoExtension = "file";

		// Act
		String coverFile = presenter.getCoverFileWithTheSameName(anyFilenameWithNoExtension);

		// Assert
		assertEquals("", coverFile);
	}

	@Test
	public void getCoverFileWithTheSameName_fileThatDoesntExist_returnsEmpty(){
		// Arrange
		MainPresenter presenter = makePresenter();
		String aFileThatDoesntExist = "file.pdf";

		// Act
		String coverFile = presenter.getCoverFileWithTheSameName(aFileThatDoesntExist);

		// Assert
		assertEquals("", coverFile);
	}

	@ParameterizedTest
	@CsvSource({"file.pdf, file.jpg", "file.pdf, file.jpeg", "file.pdf, file.png"})
	public void getCoverFileWithTheSameName_fileThatExistWithAnImageInSameFolder_returnsImageName(String fileThatExists, String imageFileWithSameName){
	    // Arrange
		StorageProvider storageProviderFake = mock(StorageProvider.class);
		MainPresenter presenter = makePresenter(storageProviderFake);
		when(storageProviderFake.exists(imageFileWithSameName)).thenReturn(true);

	    // Act
		String coverFile = presenter.getCoverFileWithTheSameName(fileThatExists);

	    // Assert
		assertEquals(imageFileWithSameName, coverFile);
	}

	@Test
	public void showInitialDialog_beforeFirstTime_returnsTrue(){
	    // Arrange
		PreferenceProviderFake viewPreferencesProvider = new PreferenceProviderFake();
		MainPresenter presenter = makePresenter(viewPreferencesProvider, null, null);

	    // Act
		boolean showDialog = presenter.showInitialDialog();

	    // Assert
		assertEquals(true, showDialog);
	}

	@Test
	public void showInitialDialog_afterFirstTime_returnsFalse(){
	    // Arrange
		PreferenceProviderFake viewPreferencesProvider = new PreferenceProviderFake();
		MainPresenter presenter = makePresenter(viewPreferencesProvider, null, null);
		presenter.initialDialogRead();

		// Act
		boolean showDialog = presenter.showInitialDialog();

	    // Assert
		assertEquals(false, showDialog);
	}

	@Test
	public void initialDialogRead_firstTimeUserOpensTheApp_saveItOnPreferences(){
		// Arrange
		PreferenceProviderFake viewPreferencesProvider = new PreferenceProviderFake();
		MainPresenter presenter = makePresenter(viewPreferencesProvider, null, null);
		assertFalse(viewPreferencesProvider.getBoolean(PreferencesKeys.FIRST_TIME_APP));

		// Act
		presenter.initialDialogRead();

		// Assert
		boolean firstTime = viewPreferencesProvider.getBoolean(PreferencesKeys.FIRST_TIME_APP);
		assertEquals(false, firstTime);
	}

	@Test
	public void updateRecentFolder_nullFilename_saveNothing(){
		// Arrange
		PreferenceProviderFake sharedPreferencesProvider = new PreferenceProviderFake();
		MainPresenter presenter = makePresenter(null, sharedPreferencesProvider, null);
		String aNullFilename = null;

		// Act
		presenter.updateRecentFolder(aNullFilename);

		// Assert
		String recentFolder = sharedPreferencesProvider.getString(PreferencesKeys.PATH);
		assertEquals(null, recentFolder);
	}

	@Test
	public void updateRecentFolder_emptyFilename_saveNothing(){
	    // Arrange
		PreferenceProviderFake sharedPreferencesProvider = new PreferenceProviderFake();
		MainPresenter presenter = makePresenter(null, sharedPreferencesProvider, null);
		String anEmptyFilename = null;

	    // Act
		presenter.updateRecentFolder(anEmptyFilename);

	    // Assert
		String recentFolder = sharedPreferencesProvider.getString(PreferencesKeys.PATH);
		assertEquals(null, recentFolder);
	}

	@Test
	public void updateRecentFolder_firstTimeOfValidFilename_saveTheFolderOfTheFilename(){
		// Arrange
		PreferenceProviderFake sharedPreferencesProvider = new PreferenceProviderFake();
		MainPresenter presenter = makePresenter(null, sharedPreferencesProvider, null);
		String anyValidFilenamePath = "path/to/folder/file";

		// Act
		presenter.updateRecentFolder(anyValidFilenamePath);

		// Arrange
		String recentFolder = sharedPreferencesProvider.getString(PreferencesKeys.PATH);
		assertEquals("path/to/folder/", recentFolder);
	}

	@Test
	public void updateRecentFolder_nonFirstTimeOfValidFilename_saveTheFolderOfTheNewFilename(){
	    // Arrange
		PreferenceProviderFake sharedPreferencesProvider = new PreferenceProviderFake();
		MainPresenter presenter = makePresenter(null, sharedPreferencesProvider, null);
		String anyValidFilenamePath = "path/to/folder/file";
		presenter.updateRecentFolder(anyValidFilenamePath);
		String anotherValidFilenamePath = "path/to/another/folder/file";

	    // Act
		presenter.updateRecentFolder(anotherValidFilenamePath);

	    // Assert
		String recentFolder = sharedPreferencesProvider.getString(PreferencesKeys.PATH);
		assertEquals("path/to/another/folder/", recentFolder);
	}

	@Test
	public void getRecentFolder_noRecentFolder_returnsExternalStorageFolder(){
	    // Arrange
		PreferenceProviderFake sharedPreferencesProvider = new PreferenceProviderFake();
		StorageProvider storageProvider = mock(StorageProvider.class);
		MainPresenter presenter = makePresenter(null, sharedPreferencesProvider, storageProvider);
		String externalStorageDirectory = "externalStorageDirectory";
		when(storageProvider.getExternalStorageDirectory()).thenReturn(externalStorageDirectory);

	    // Act
		String recentFolder = presenter.getRecentFolder();

	    // Assert
		assertEquals(externalStorageDirectory, recentFolder);
	}

	@Test
	public void getRecentFolder_haveRecentFolder_returnsThatRecentFolder(){
	    // Arrange
		PreferenceProviderFake sharedPreferencesProvider = new PreferenceProviderFake();
		MainPresenter presenter = makePresenter(null, sharedPreferencesProvider, null);
		String anyValidFilenamePath = "path/to/folder/file";

	    // Act
		presenter.updateRecentFolder(anyValidFilenamePath);

	    // Assert
		String recentFolder = presenter.getRecentFolder();
		assertEquals("path/to/folder/", recentFolder);
	}

	@Test
	public void userPrefersToUsePicture_userPreferedToChoosePicture_returnsTrue(){
	    // Arrange
		PreferenceProviderFake sharedPreferencesProvider = new PreferenceProviderFake();
		MainPresenter presenter = makePresenter(null, sharedPreferencesProvider, null);
		sharedPreferencesProvider.save(PreferencesKeys.CHOOSE_PICTURE, true);

	    // Act
		boolean userPrefersToChoosePicture = presenter.userPrefersToChoosePicture();

	    // Assert
		assertEquals(true, userPrefersToChoosePicture);
	}

	@Test
	public void userPrefersToUsePicture_userNotPreferedToChoosePicture_returnsFalse(){
	    // Arrange
		PreferenceProviderFake sharedPreferencesProvider = new PreferenceProviderFake();
		MainPresenter presenter = makePresenter(null, sharedPreferencesProvider, null);
		sharedPreferencesProvider.save(PreferencesKeys.CHOOSE_PICTURE, false);

	    // Act
		boolean userPrefersToChoosePicture = presenter.userPrefersToChoosePicture();

	    // Assert
		assertEquals(false, userPrefersToChoosePicture);
	}

	//<editor-fold desc="Factories">
	private MainPresenter makePresenter(){
		return makePresenter(null, null, null);
	}

	private MainPresenter makePresenter(StorageProvider storageProvider){
		return new MainPresenterImpl(null, null, storageProvider);
	}

	private MainPresenter makePresenter(PreferenceProvider viewPreferencesProvider, PreferenceProvider sharedPreferencesProvider, StorageProvider storageProvider){
		viewPreferencesProvider = viewPreferencesProvider == null ? new PreferenceProviderFake() : viewPreferencesProvider;
		sharedPreferencesProvider = sharedPreferencesProvider == null ? new PreferenceProviderFake() : sharedPreferencesProvider;
		storageProvider = storageProvider == null ? mock(StorageProvider.class) : storageProvider;
		return new MainPresenterImpl(viewPreferencesProvider, sharedPreferencesProvider, storageProvider);
	}
	//</editor-fold>

}
