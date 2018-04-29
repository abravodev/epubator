package it.iiizio.epubator.domain.constants;

public class ConversionStatus {

    public static final int NOT_STARTED = 0;
	public static final int LOADING_FILE = 1;
	public static final int IN_PROGRESS = 2;
	public static final int SUCCESS = 3;
	public static final int FILE_NOT_FOUND = 4;
	public static final int CANNOT_READ_PDF = 5;
	public static final int CANNOT_WRITE_EPUB = 6;
	public static final int EXTRACTION_ERROR = 7;
	public static final int CONVERSION_STOPPED_BY_USER = 8;
	public static final int OUT_OF_MEMORY_ERROR = 9;
}
