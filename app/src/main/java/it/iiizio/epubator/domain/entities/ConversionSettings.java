package it.iiizio.epubator.domain.entities;

import java.io.Serializable;
import java.util.Date;

import it.iiizio.epubator.domain.utils.FileHelper;

public class ConversionSettings implements Serializable {

	//<editor-fold desc="Attributes">
	private static final String EPUB_EXT = " - ePUBator.epub";
	private static final String OLD_EXT = " - ePUBator.old";
	private static final String TEMP_EXT = " - ePUBator.tmp";

	public final String pdfFilename;
    public final String filename;
    public final String epubFilename;
    public final String oldFilename;
    public final String tempFilename;
    public final String coverFile;
    public final String temporalPath;
    private final ConversionPreferences preferences;
	//</editor-fold>

	//<editor-fold desc="Constructors">
	public ConversionSettings(ConversionPreferences preferences, String outputDirectory, String temporalPath,
		  String pdfFilename, String coverFile){

		this.filename = FileHelper.getFilenameFromPdfFile(pdfFilename);
        this.epubFilename = outputDirectory + this.filename + EPUB_EXT;
        this.oldFilename = temporalPath + this.filename + OLD_EXT;
        this.tempFilename = temporalPath + this.filename + TEMP_EXT;

        this.temporalPath = temporalPath;
        this.pdfFilename = pdfFilename;
        this.coverFile = coverFile;
        this.preferences = preferences;
    }
	//</editor-fold>

	//<editor-fold desc="Methods">
    public String getTitle(){
        return filename.replaceAll("[^\\p{Alnum}]", " ");
    }

    public String generateBookId(){
        return getTitle() + " - " + new Date().hashCode();
    }

    public ConversionPreferences getPreferences() {
        return preferences;
    }
	//</editor-fold>
}
