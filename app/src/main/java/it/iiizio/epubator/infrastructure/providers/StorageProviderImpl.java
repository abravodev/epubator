package it.iiizio.epubator.infrastructure.providers;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class StorageProviderImpl implements StorageProvider {

	//<editor-fold desc="Attributes">
	private final Context context;
	private static final int BUFFER_SIZE = 2048;
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

	@Override
	public void save(InputStream inputStream, String directory, String filename) throws IOException {
		BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream, BUFFER_SIZE);
		File outputFile = new File(directory, filename);
		FileOutputStream outputStream = new FileOutputStream(outputFile);
		byte[] buffer = new byte[BUFFER_SIZE];
		int len;
		BufferedOutputStream dest = new BufferedOutputStream(outputStream, BUFFER_SIZE);
		while ((len = bufferedInputStream.read(buffer, 0, BUFFER_SIZE)) != -1) {
			dest.write(buffer, 0, len);
		}
		dest.flush();
		dest.close();
		bufferedInputStream.close();
	}

	@Override
	public String read(InputStream inputStream) throws IOException {
		StringBuilder text = new StringBuilder();
		Reader inputStreamReader = new InputStreamReader(inputStream);
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader, BUFFER_SIZE);
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			text.append(line);
		}
		return text.toString();
	}

	@Override
	public void addText(String filename, String text) throws IOException {
		FileWriter writer = new FileWriter(filename);
		writer.append(text);
		writer.flush();
		writer.close();
	}
	//</editor-fold>
}
