package it.iiizio.epubator.infrastructure.providers;

import java.io.IOException;
import java.io.InputStream;

public interface FileProvider {

	boolean exists(String filename);

	boolean rename(String oldName, String newName);

	boolean remove(String filename);

	void removeAllFromDirectory(String directoryPath);

	String getFile(String directory, String filename);

	void save(InputStream inputStream, String directory, String filename) throws IOException;

	String read(InputStream inputStream) throws IOException;

	void addText(String filename, String text) throws IOException;

	boolean folderIsWritable(String folder);

}
