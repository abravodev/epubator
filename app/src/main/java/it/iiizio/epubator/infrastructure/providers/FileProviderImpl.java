package it.iiizio.epubator.infrastructure.providers;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharSink;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class FileProviderImpl implements FileProvider {

	@Override
	public String getFile(String directory, String filename) {
		return new File(directory, filename).getAbsolutePath();
	}

	@Override
	public void save(InputStream inputStream, String directory, String filename) throws IOException {
		byte[] buffer = ByteStreams.toByteArray(inputStream);
		File outputFile = new File(directory, filename);
		Files.write(buffer, outputFile);
	}

	@Override
	public String read(InputStream inputStream) throws IOException {
		Charset charset = Charset.defaultCharset(); // TODO: We should get it from somewhere
		String text = CharStreams.toString(new InputStreamReader(inputStream, charset));
		Closeables.close(inputStream, false);
		return text;
	}

	@Override
	public void addText(String filename, String text) throws IOException {
		File file = new File(filename);
		Charset charset = Charset.defaultCharset(); // TODO: We should get it from somewhere
		CharSink charSink = Files.asCharSink(file, charset, FileWriteMode.APPEND);
		charSink.write(text);
	}

	@Override
	public boolean folderIsWritable(String folder) {
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
}
