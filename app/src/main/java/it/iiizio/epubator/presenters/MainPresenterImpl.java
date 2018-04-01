package it.iiizio.epubator.presenters;

import java.io.File;

import it.iiizio.epubator.model.utils.FileHelper;

public class MainPresenterImpl implements MainPresenter {

    @Override
    public String getCoverFileWithTheSameName(String filename) {
        String name = FileHelper.getPathWithoutExtension(filename);
        String coverFile = "";

        if(new File(name + ".png").exists()) {
            coverFile = name + ".png";
        } else if(new File(name + ".jpg").exists()) {
            coverFile = name + ".jpg";
        } else if(new File(name + ".jpeg").exists()) {
            coverFile = name + ".jpeg";
        }

        return coverFile;
    }

}
