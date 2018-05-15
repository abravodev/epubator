package it.iiizio.epubator.presentation.presenters;

import java.io.IOException;
import java.io.InputStream;

import it.iiizio.epubator.infrastructure.providers.FileProvider;

public class InfoPresenterImpl implements InfoPresenter {

	private final FileProvider fileProvider;

	public InfoPresenterImpl(FileProvider fileProvider) {
		this.fileProvider = fileProvider;
	}

	@Override
    public String getInfo(InputStream is) {
		try {
			return fileProvider.read(is);
		} catch (IOException e) {
			System.err.println(e);
			return "";
		}
	}
}
