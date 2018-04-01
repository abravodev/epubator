package it.iiizio.epubator.model.utils;

import java.io.File;

public class FileHelper {

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

}
