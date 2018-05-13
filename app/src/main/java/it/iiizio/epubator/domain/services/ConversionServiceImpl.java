package it.iiizio.epubator.domain.services;

import it.iiizio.epubator.domain.utils.FileHelper;
import it.iiizio.epubator.infrastructure.providers.StorageProvider;

public class ConversionServiceImpl implements ConversionService {

	private final StorageProvider storageProvider;

	public ConversionServiceImpl(StorageProvider storageProvider) {
		this.storageProvider = storageProvider;
	}

	@Override
	public String getOutputDirectory(String pdfFilename, boolean saveOnDownloadDirectory) {
		if(saveOnDownloadDirectory){
			return storageProvider.getDownloadDirectory();
		}

		String path = FileHelper.getDirectoryFromPdfFile(pdfFilename);
		boolean writable = storageProvider.folderIsWritable(path);
		if(writable){
			return path;
		}

		return storageProvider.getDownloadDirectory();
	}
}
