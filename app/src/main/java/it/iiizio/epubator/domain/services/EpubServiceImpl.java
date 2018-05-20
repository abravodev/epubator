package it.iiizio.epubator.domain.services;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import it.iiizio.epubator.domain.constants.ZipFileConstants;
import it.iiizio.epubator.domain.entities.EBook;
import it.iiizio.epubator.domain.utils.PdfXmlParser;
import it.iiizio.epubator.infrastructure.providers.FileProvider;

public class EpubServiceImpl implements EpubService {

	//<editor-fold desc="Attributes">
	private final FileProvider fileProvider;
	private final PdfXmlParser pdfParser;
	//</editor-fold>

	//<editor-fold desc="Constructors">
	public EpubServiceImpl(FileProvider fileProvider, PdfXmlParser parser) {
		this.fileProvider = fileProvider;
		this.pdfParser = parser;
	}
	//</editor-fold>

	//<editor-fold desc="Methods">
	@Override
	public EBook getBook(String filename) throws IOException {
		ZipFile epubFile = new ZipFile(filename);
		List<String> pages = getPages(epubFile);
		EBook book = new EBook(epubFile, pages);

		NodeList nodes = getNodes(epubFile);
		if (nodes != null) {
			for (int i = 0; i < nodes.getLength(); i++) {
				Element chapterElement = (Element) nodes.item(i);
				String chapterTitle = chapterElement.getTextContent().trim();
				book.addChapter(chapterTitle);
				String anchor = pdfParser.getAnchor(chapterElement);
				book.addAnchor(ZipFileConstants.anchor(anchor));
			}
		}

		return book;
	}

	@Override
	public void saveHtmlPage(String htmlFile, String htmlText) throws IOException {
		fileProvider.addText(htmlFile, htmlText);
	}

	@Override
	public void saveImages(ZipFile epubFile, String htmlPage, String imageDirectory) {
		List<String> images = pdfParser.extractImages(htmlPage);
		for (String image: images){
			saveImage(epubFile, imageDirectory, image);
		}
	}

	@Override
	public String getHtmlPage(ZipFile epubFile, String htmlFile) throws IOException {
		return getElement(epubFile, htmlFile);
	}

	private List<String> getPages(ZipFile epubFile) {
		List<String> pages = new ArrayList<>();
		Enumeration<? extends ZipEntry> entries = epubFile.entries();
		while(entries.hasMoreElements()){
			ZipEntry entry = entries.nextElement();
			String name = entry.getName();
			if (name.endsWith(".html")) {
				pages.add(name);
			}
		}

		return pages;
	}

	private void saveImage(ZipFile epubFile, String imageDirectory, String imageName) {
		try {
			InputStream entry = getEntry(epubFile, ZipFileConstants.image(imageName));
			fileProvider.save(entry, imageDirectory, imageName);
		} catch (IOException e) {
			String errorMessage = String.format("Failed to fetch image '%s'", imageName);
			System.err.println(errorMessage);
		}
	}

	private NodeList getNodes(ZipFile epubFile) throws IOException {
		String toc = getTOC(epubFile);
		return pdfParser.getNavigationPoints(toc);
	}

	private String getTOC(ZipFile epubFile) throws IOException {
		return getElement(epubFile, ZipFileConstants.TOC);
	}

	private String getElement(ZipFile epubFile, String elementKey) throws IOException {
		InputStream inputStream = getEntry(epubFile, elementKey);
		return fileProvider.read(inputStream);
	}

	private InputStream getEntry(ZipFile zipFile, String entryName) throws IOException {
		ZipEntry entry = zipFile.getEntry(entryName);
		return zipFile.getInputStream(entry);
	}
	//</editor-fold>
}
