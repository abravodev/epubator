package it.iiizio.epubator.presentation.presenters;

import java.io.File;

import it.iiizio.epubator.domain.entities.ConversionSettings;

public class ConvertPresenterImpl implements ConvertPresenter {

    @Override
    public boolean deleteTemporalFile(ConversionSettings settings) {
        new File(settings.tempFilename).delete();
        if (new File(settings.oldFilename).exists()) {
            new File(settings.oldFilename).renameTo(new File(settings.epubFilename));
            return true;
        }
        return false;
    }

    @Override
    public void saveEpub(ConversionSettings settings) {
        new File(settings.tempFilename).renameTo(new File(settings.epubFilename));
        new File(settings.oldFilename).delete();
    }

}
