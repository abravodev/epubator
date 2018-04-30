package it.iiizio.epubator.presentation.dto;

import java.io.Serializable;
import java.util.Date;

import it.iiizio.epubator.domain.utils.FileHelper;

public class ConversionSettings implements Serializable {

    public final String pdfFilename;
    public final String filename;
    public final String epubFilename;
    public final String oldFilename;
    public final String tempFilename;
    public final String coverFile;
    public final String temporalPath;
    private final ConversionPreferences preferences;

    private final String EPUB_EXT = " - ePUBator.epub";
    private final String OLD_EXT = " - ePUBator.old";
    private final String TEMP_EXT = " - ePUBator.tmp";

    public ConversionSettings(ConversionPreferences preferences, String pdfFilename, String temporalPath,
                              String downloadHistory, String coverFile){
        String[] parts = FileHelper.getPathAndFilenameOfPdf(pdfFilename);
        String path = parts[0];
        this.filename = parts[1];

        this.epubFilename = getEpubFilename(path, this.filename, preferences.saveOnDownloadDirectory, downloadHistory);
        this.oldFilename = temporalPath + this.filename + OLD_EXT;
        this.tempFilename = temporalPath + this.filename + TEMP_EXT;

        this.temporalPath = temporalPath;
        this.pdfFilename = pdfFilename;
        this.coverFile = coverFile;
        this.preferences = preferences;
    }

    /**
     * Save ePUB in the Download folder as user choice or if PDF folder is not writable
     * @param path
     * @param filename
     * @param saveOnDownloadDirectory
     * @return
     */
    private String getEpubFilename(String path, String filename, boolean saveOnDownloadDirectory, String downloadHistory){
        boolean writable = FileHelper.folderIsWritable(path);
        String outputDirectory = (saveOnDownloadDirectory || !writable) ? downloadHistory: path;
        return outputDirectory + filename + EPUB_EXT;
    }

    public String getTitle(){
        return filename.replaceAll("[^\\p{Alnum}]", " ");
    }

    public String getBookId(){
        return getTitle() + " - " + new Date().hashCode();
    }

    public ConversionPreferences getPreferences() {
        return preferences;
    }
}
