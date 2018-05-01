package it.iiizio.epubator.domain.services;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import it.iiizio.epubator.domain.entities.Book;
import it.iiizio.epubator.domain.utils.XMLParser;

public class EpubServiceImpl implements EpubService {

	private static final int BUFFERSIZE = 2048;

	@Override
	public Book getBook(ZipFile epubFile) throws IOException {
		Book book = new Book();

		XMLParser parser = new XMLParser();
		NodeList nodes = getNodes(epubFile, parser);
		if (nodes != null) {
			// looping through all item nodes <item>
			for (int i = 0; i < nodes.getLength(); i++) {
				Element chapterElement = (Element) nodes.item(i);
				String chapterTitle = chapterElement.getTextContent().trim();
				book.addChapter(chapterTitle);
				Element anchorElement = (Element) chapterElement.getChildNodes().item(3);
				String anchor = parser.getValue(anchorElement, "src");
				book.addAnchor("OEBPS/" + anchor);
			}
		}

		return book;
	}

	@Override
	public void saveHtmlPage(File htmlFile, String htmlText) throws IOException {
		FileWriter writer = new FileWriter(htmlFile);
		writer.append(htmlText);
		writer.flush();
		writer.close();
	}

	@Override
	public void saveImages(ZipFile epubFile, String htmlPage, File imageDirectory) {
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
	public List<String> getPages(ZipFile epubFile) {
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

	@Override
	public String getHtmlPage(ZipFile epubFile, String htmlFile) throws IOException {
		String htmlPage = getElement(epubFile, htmlFile);
		return htmlPage
			.replace("<body>", "<body bgcolor=\"Black\"><font color=\"White\">")
			.replace("</body>", "</font></body>");
	}

	private void saveImage(ZipFile epubFile, File imageDirectory, String imageName) throws IOException {
		ZipEntry entry = epubFile.getEntry("OEBPS/" + imageName);
		BufferedInputStream in = new BufferedInputStream(epubFile.getInputStream(entry), BUFFERSIZE);
		FileOutputStream out = new FileOutputStream(new File(imageDirectory + "/" + imageName));
		byte[] buffer = new byte[BUFFERSIZE];
		int len;
		BufferedOutputStream dest = new BufferedOutputStream(out, BUFFERSIZE);
		while ((len = in.read(buffer, 0, BUFFERSIZE)) != -1) {
			dest.write(buffer, 0, len);
		}
		dest.flush();
		dest.close();
		in.close();
	}

	private NodeList getNodes(ZipFile epubFile, XMLParser parser) throws IOException {
		String toc = getTOC(epubFile);
		Document doc = parser.getDomElement(toc);
		if(doc == null){
			return null;
		}
		doc.normalize();
		return doc.getElementsByTagName("navPoint");
	}

	private String getTOC(ZipFile epubFile) throws IOException {
		return getElement(epubFile, "OEBPS/toc.ncx");
	}

	private String getElement(ZipFile epubFile, String elementKey) throws IOException {
		StringBuilder textElement = new StringBuilder();
		Reader inputStreamReader = new InputStreamReader(epubFile.getInputStream(epubFile.getEntry(elementKey)));
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader, BUFFERSIZE);
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			textElement.append(line);
		}
		return textElement.toString();
	}
}
