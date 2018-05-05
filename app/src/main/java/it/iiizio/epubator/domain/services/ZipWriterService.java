package it.iiizio.epubator.domain.services;

public interface ZipWriterService {

	boolean create(String filename);

	boolean addText(String filename, String text);

	boolean addText(String filename, String text, boolean store);

	boolean addImage(String filename, byte[] image);

	boolean close();
}
