package it.iiizio.epubator.presentation.presenters;

import it.iiizio.epubator.presentation.dto.ConversionSettings;

public interface ConvertPresenter {

    boolean deleteTemporalFile(ConversionSettings settings);

    void saveEpub(ConversionSettings settings);

}
