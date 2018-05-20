package it.iiizio.epubator.presentation.presenters;

import java.io.IOException;
import java.util.zip.ZipFile;

import it.iiizio.epubator.domain.constants.PreferencesKeys;
import it.iiizio.epubator.domain.entities.EBook;
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
    public EBook getBook(String filename) throws IOException {
		EBook book = epubService.getBook(filename);
		if(book.getPagesCount()==0){
			throw new IOException("Book has no pages");
		}
		return book;
    }

    @Override
    public String getHtmlPage(ZipFile epubFile, String htmlFile) throws IOException {
        return epubService.getHtmlPage(epubFile, htmlFile);
    }

    @Override
    public String saveHtmlPage(ZipFile epubFile, String htmlText) throws IOException {
		removeFilesFromTemporalDirectory();
		saveHtmlPage(htmlText);
		saveImages(epubFile, htmlText);
		return getHtmlPageFilename();
    }

	@Override
	public boolean showImages() {
		return preferenceProvider.getBoolean(PreferencesKeys.SHOW_IMAGES_ON_VERIFY, true);
	}

	@Override
	public void removeFilesFromTemporalDirectory() {
		storageProvider.removeAllFromDirectory(storageProvider.getFileDirectory());
	}

	@Override
	public void closeBook(EBook book) throws IOException {
		book.getFile().close();
	}

	private void saveHtmlPage(String htmlText) throws IOException {
		String htmlFile = getHtmlPageFilename();
		epubService.saveHtmlPage(htmlFile, htmlText);
	}

	private void saveImages(ZipFile epubFile, String htmlPage) {
		String imageDirectory = storageProvider.getFileDirectory();
		epubService.saveImages(epubFile, htmlPage, imageDirectory);
	}

	private String getHtmlPageFilename() {
		return storageProvider.getFile(storageProvider.getFileDirectory(), "page.html");
	}
	//</editor-fold>

}
