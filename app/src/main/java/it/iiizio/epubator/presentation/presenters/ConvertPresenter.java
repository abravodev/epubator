package it.iiizio.epubator.presentation.presenters;

import it.iiizio.epubator.domain.entities.ConversionSettings;

public interface ConvertPresenter {

    boolean deleteTemporalFile(ConversionSettings settings);

    void saveEpub(ConversionSettings settings);

	ConversionSettings getConversionSettings(String pdfFilename, String coverImage);

}
