package it.iiizio.epubator.infrastructure.providers;

import android.content.Context;
import android.os.Environment;

public class StorageProviderImpl extends FileProviderImpl implements StorageProvider  {

	//<editor-fold desc="Attributes">
	private final Context context;
	//</editor-fold>

	//<editor-fold desc="Constructors">
	public StorageProviderImpl(Context context) {
		this.context = context;
	}
	//</editor-fold>

	//<editor-fold desc="Methods">
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
	public String getFileDirectory() {
		return context.getFilesDir().getAbsolutePath();
	}
	//</editor-fold>
}
