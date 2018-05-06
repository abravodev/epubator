package it.iiizio.epubator.infrastructure.providers;

public interface StorageProvider {

	String getExternalStorageDirectory();

	String getExternalCacheDirectory();

	String getDownloadDirectory();

	boolean exists(String filename);

	boolean rename(String oldName, String newName);

	boolean remove(String filename);

	void removeAllFromDirectory(String directoryPath);

	String getFileDirectory();

	String getFile(String directory, String filename);
}
