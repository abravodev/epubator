package it.iiizio.epubator.infrastructure.providers;

public interface StorageProvider {

	String getExternalStorageDirectory();

	String getExternalCacheDirectory();

	String getDownloadDirectory();

	boolean exists(String filename);
}
