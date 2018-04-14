package it.iiizio.epubator.domain.utils;

import java.io.File;
import java.io.IOException;

public class FileHelper {

    private static final String PDF_EXT = ".pdf";

    public static String[] getPathAndFilenameOfPdf(String pdfFilename){
        String noExt = pdfFilename.substring(0, pdfFilename.lastIndexOf(PDF_EXT));
        String path = noExt.substring(0, noExt.lastIndexOf('/', noExt.length()) + 1);
        String filename = noExt.substring(noExt.lastIndexOf("/") + 1, noExt.length());

        return new String[]{ path, filename };
    }

    public static void deleteFilesFromDirectory(File directory){
        if (directory != null && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if(files != null) {
                for(File f : files) {
                    f.delete();
                }
            }
        }
    }

    public static boolean folderIsWritable(String folder){
        boolean writable = false;
        try {
            File checkFile = new File(folder);
            writable = checkFile.createNewFile();
            checkFile.delete();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return writable;
    }

    public static String getPathWithoutExtension(String filename){
        return filename.substring(0, filename.lastIndexOf("."));
    }

}
