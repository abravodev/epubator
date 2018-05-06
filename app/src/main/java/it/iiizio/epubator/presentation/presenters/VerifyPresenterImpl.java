package it.iiizio.epubator.presentation.presenters;

import java.io.IOException;
import java.util.zip.ZipFile;

import it.iiizio.epubator.domain.constants.PreferencesKeys;
import it.iiizio.epubator.domain.entities.Book;
import it.iiizio.epubator.domain.services.EpubService;
import it.iiizio.epubator.infrastructure.providers.PreferenceProvider;
import it.iiizio.epubator.infrastructure.providers.StorageProvider;

public class VerifyPresenterImpl implements VerifyPresenter {

	//<editor-fold desc="Attributes">
	private final EpubService epubService;
    private final PreferenceProvider preferenceProvider;
    private final StorageProvider storageProvider;
	//</editor-fold>

	//<editor-fold desc="Constructors">
	public VerifyPresenterImpl(EpubService epubService, PreferenceProvider preferenceProvider, StorageProvider storageProvider) {
		this.preferenceProvider = preferenceProvider;
		this.epubService = epubService;
		this.storageProvider = storageProvider;
	}
	//</editor-fold>

	//<editor-fold desc="Methods">
	@Override
    public Book getBook(ZipFile epubFile) throws IOException {
		return epubService.getBook(epubFile);
    }

    @Override
    public String getHtmlPage(ZipFile epubFile, String htmlFile) throws IOException {
        return epubService.getHtmlPage(epubFile, htmlFile);
    }

    @Override
    public void saveImages(ZipFile epubFile, String htmlPage) {
		String imageDirectory = storageProvider.getFileDirectory();
        epubService.saveImages(epubFile, htmlPage, imageDirectory);
    }

    @Override
    public void saveHtmlPage(String htmlText) throws IOException {
		String htmlFile = getHtmlPageFilename();
		epubService.saveHtmlPage(htmlFile, htmlText);
    }

	@Override
	public String getHtmlPageFilename() {
		return storageProvider.getFile(storageProvider.getFileDirectory(), "page.html");
	}

	@Override
	public boolean showImages() {
		return preferenceProvider.getBoolean(PreferencesKeys.SHOW_IMAGES_ON_VERIFY, true);
	}

	@Override
	public void removeFilesFromTemporalDirectory() {
		storageProvider.removeAllFromDirectory(storageProvider.getFileDirectory());
	}
	//</editor-fold>

}
