package it.iiizio.epubator.infrastructure.providers;

public interface StorageProvider extends FileProvider {

	String getExternalStorageDirectory();

	String getExternalCacheDirectory();

	String getDownloadDirectory();

	String getFileDirectory();
}
