package it.iiizio.epubator.infrastructure.providers;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public class StorageProviderImpl implements StorageProvider {

	private final Context context;

	public StorageProviderImpl(Context context) {
		this.context = context;
	}

	@Override
	public String getExternalStorageDirectory() {
		return Environment.getExternalStorageDirectory().getPath();
	}

	@Override
	public String getExternalCacheDirectory() {
		return context.getExternalCacheDir() + "/";
	}

	@Override
	public String getDownloadDirectory() {
		return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/";
	}

	@Override
	public boolean exists(String filename) {
		return new File(filename).exists();
	}

	@Override
	public boolean rename(String oldFilename, String newFilename) {
		return new File(oldFilename).renameTo(new File(newFilename));
	}

	@Override
	public boolean remove(String filename) {
		return new File(filename).delete();
	}

	@Override
	public void removeAllFromDirectory(String directoryPath) {
		File directory = new File(directoryPath);
		if (!directory.isDirectory()) {
			return;
		}
		File[] files = directory.listFiles();
		if(files != null) {
			for(File f : files) {
				f.delete();
			}
		}
	}

	@Override
	public String getFileDirectory() {
		return context.getFilesDir().getAbsolutePath();
	}

	@Override
	public String getFile(String directory, String filename) {
		return new File(directory, filename).getAbsolutePath();
	}
}
