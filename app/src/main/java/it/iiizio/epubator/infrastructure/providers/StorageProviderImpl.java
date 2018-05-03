package it.iiizio.epubator.infrastructure.providers;

import android.content.Context;
import android.os.Environment;

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
}
