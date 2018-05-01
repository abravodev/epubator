package it.iiizio.epubator.presentation.presenters;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipFile;

import it.iiizio.epubator.domain.entities.Book;
import it.iiizio.epubator.domain.services.EpubService;
import it.iiizio.epubator.domain.services.EpubServiceImpl;

public class VerifyPresenterImpl implements VerifyPresenter {

    private final EpubService epubService;

	public VerifyPresenterImpl() {
		this.epubService = new EpubServiceImpl();
	}

	@Override
    public Book getBook(ZipFile epubFile) throws IOException {
		return epubService.getBook(epubFile);
    }

    @Override
    public List<String> getPages(ZipFile epubFile) {
        return epubService.getPages(epubFile);
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

}
