package it.iiizio.epubator.presentation.presenters;

import java.io.IOException;
import java.io.InputStream;

import it.iiizio.epubator.infrastructure.providers.FileProvider;

public class LicensePresenterImpl implements LicensePresenter {

	private final FileProvider fileProvider;

	public LicensePresenterImpl(FileProvider fileProvider) {
		this.fileProvider = fileProvider;
	}

	@Override
    public String getLicenseInfo(InputStream is) {
        try {
        	return fileProvider.read(is);
        } catch (IOException e) {
			System.err.println(e);
            return "";
        }
    }
}
