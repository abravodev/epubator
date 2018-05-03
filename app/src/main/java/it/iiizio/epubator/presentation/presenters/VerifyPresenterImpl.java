package it.iiizio.epubator.presentation.presenters;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

import it.iiizio.epubator.domain.constants.PreferencesKeys;
import it.iiizio.epubator.domain.entities.Book;
import it.iiizio.epubator.domain.services.EpubService;
import it.iiizio.epubator.infrastructure.providers.PreferenceProvider;

public class VerifyPresenterImpl implements VerifyPresenter {

    private final EpubService epubService;
    private final PreferenceProvider preferenceProvider;

	public VerifyPresenterImpl(EpubService epubService, PreferenceProvider preferenceProvider) {
		this.preferenceProvider = preferenceProvider;
		this.epubService = epubService;
	}

	@Override
    public Book getBook(ZipFile epubFile) throws IOException {
		return epubService.getBook(epubFile);
    }

    @Override
    public String getHtmlPage(ZipFile epubFile, String htmlFile) throws IOException {
        return epubService.getHtmlPage(epubFile, htmlFile);
    }

    @Override
    public void saveImages(ZipFile epubFile, String htmlPage, File imageDirectory) {
        epubService.saveImages(epubFile, htmlPage, imageDirectory);
    }

    @Override
    public void saveHtmlPage(File htmlFile, String htmlText) throws IOException {
		epubService.saveHtmlPage(htmlFile, htmlText);
    }

	@Override
	public boolean showImages() {
		return preferenceProvider.getBoolean(PreferencesKeys.SHOW_IMAGES_ON_VERIFY, true);
	}

}
