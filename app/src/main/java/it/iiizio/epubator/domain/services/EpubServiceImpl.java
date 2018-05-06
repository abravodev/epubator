package it.iiizio.epubator.domain.services;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import it.iiizio.epubator.domain.entities.Book;
import it.iiizio.epubator.domain.utils.PdfXmlParser;
import it.iiizio.epubator.infrastructure.providers.StorageProvider;

public class EpubServiceImpl implements EpubService {

	//<editor-fold desc="Attributes">
	private final StorageProvider storageProvider;
	private final PdfXmlParser parser;
	//</editor-fold>

	//<editor-fold desc="Constructors">
	public EpubServiceImpl(StorageProvider storageProvider, PdfXmlParser parser) {
		this.storageProvider = storageProvider;
		this.parser = parser;
	}
	//</editor-fold>

	//<editor-fold desc="Methods">
	@Override
	public Book getBook(ZipFile epubFile) throws IOException {
		List<String> pages = getPages(epubFile);
		Book book = new Book(pages);

		NodeList nodes = getNodes(epubFile);
		if (nodes != null) {
			for (int i = 0; i < nodes.getLength(); i++) {
				Element chapterElement = (Element) nodes.item(i);
				String chapterTitle = chapterElement.getTextContent().trim();
				book.addChapter(chapterTitle);
				String anchor = parser.getAnchor(chapterElement);
				book.addAnchor("OEBPS/" + anchor);
			}
		}

		return book;
	}

	@Override
	public void saveHtmlPage(String htmlFile, String htmlText) throws IOException {
		storageProvider.addText(htmlFile, htmlText);
	}

	@Override
	public void saveImages(ZipFile epubFile, String htmlPage, String imageDirectory) {
		try {
			XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = parserFactory.newPullParser();

			parser.setInput(new StringReader(htmlPage.replaceAll("&nbsp;", "")));
			int eventType = parser.getEventType();

			while (eventType != XmlPullParser.END_DOCUMENT) {
				if(eventType == XmlPullParser.START_TAG && "img".equals(parser.getName())) {
					String imageName = parser.getAttributeValue(null, "src");
					saveImage(epubFile, imageDirectory, imageName);
				}
				eventType = parser.next();
			}
		} catch (XmlPullParserException e) {
			System.err.println("XmlPullParserException in image preview");
		} catch (IOException e) {
			System.err.println("IOException in image preview");
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

	private void saveImage(ZipFile epubFile, String imageDirectory, String imageName) throws IOException {
		ZipEntry entry = epubFile.getEntry("OEBPS/" + imageName);
		storageProvider.save(epubFile.getInputStream(entry), imageDirectory, imageName);
	}

	private NodeList getNodes(ZipFile epubFile) throws IOException {
		String toc = getTOC(epubFile);
		return parser.getNavigationPoints(toc);
	}

	private String getTOC(ZipFile epubFile) throws IOException {
		return getElement(epubFile, "OEBPS/toc.ncx");
	}

	private String getElement(ZipFile epubFile, String elementKey) throws IOException {
		InputStream inputStream = epubFile.getInputStream(epubFile.getEntry(elementKey));
		return storageProvider.read(inputStream);
	}
	//</editor-fold>
}
