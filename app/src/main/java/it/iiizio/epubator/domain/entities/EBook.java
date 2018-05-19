package it.iiizio.epubator.domain.entities;

import java.util.List;
import java.util.zip.ZipFile;

public class EBook extends Book {

	private final ZipFile zipFile;

	public EBook(ZipFile zipFile, List<String> pages) {
		super(pages);
		this.zipFile = zipFile;
	}

	public ZipFile getFile() {
		return zipFile;
	}
}
