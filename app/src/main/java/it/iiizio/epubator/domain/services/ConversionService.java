package it.iiizio.epubator.domain.services;

public interface ConversionService {

	/**
	 * Save ePUB in the Download folder as user choice or if PDF folder is not writable
	 * @param pdfFilename
	 * @param saveOnDownloadDirectory
	 * @return
	 */
	String getOutputDirectory(String pdfFilename, boolean saveOnDownloadDirectory);

}
