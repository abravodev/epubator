package it.iiizio.epubator.domain.entities;

import java.io.Serializable;

public class ConversionPreferences implements Serializable {

    public boolean includeImages;
    public boolean repeatedImages;
    public int pagesPerFile;
    public int onError;
    public boolean addMarkers;
    public boolean tocFromPdf;
    public boolean showLogoOnCover;
    public boolean saveOnDownloadDirectory;

}
