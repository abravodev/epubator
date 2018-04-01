package it.iiizio.epubator.model.constants;

public class ConversionStatus {

    public static final int SUCCESS = 0;
    public static final int FILE_NOT_FOUND = 1;
    public static final int CANNOT_READ_PDF = 2;
    public static final int CANNOT_WRITE_EPUB = 3;
    public static final int EXTRACTION_ERROR = 4;
    public static final int CONVERSION_STOPPED_BY_USER = 5;
    public static final int OUT_OF_MEMORY_ERROR = 6;
}
