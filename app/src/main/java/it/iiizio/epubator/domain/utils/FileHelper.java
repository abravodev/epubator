package it.iiizio.epubator.domain.utils;

public class FileHelper {

    private static final String PDF_EXT = ".pdf";

    public static String getDirectoryFromPdfFile(String path){
		String noExt = path.substring(0, path.lastIndexOf(PDF_EXT));
		return noExt.substring(0, noExt.lastIndexOf('/', noExt.length()) + 1);
	}

	public static String getFilenameFromPdfFile(String path){
		String noExt = path.substring(0, path.lastIndexOf(PDF_EXT));
		return noExt.substring(noExt.lastIndexOf("/") + 1, noExt.length());
	}

    public static String getPathWithoutExtension(String filename){
    	int extensionPosition = filename.lastIndexOf(".");
    	if(extensionPosition<0){
    		return filename; // It's already without extension
		}
        return filename.substring(0, extensionPosition);
    }

}
