package it.iiizio.epubator.presenters;

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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import it.iiizio.epubator.model.utils.XMLParser;
import it.iiizio.epubator.model.entities.Book;

public class VerifyPresenterImpl implements VerifyPresenter {

    private static final int BUFFERSIZE = 2048;

    @Override
    public Book getBook(ZipFile epubFile) throws IOException {
        Book book = new Book();

        String toc = getTOC(epubFile);
        XMLParser parser = new XMLParser();
        Document doc = parser.getDomElement(toc);
        if (doc != null) {
            doc.normalize();
            NodeList nl = doc.getElementsByTagName("navPoint");
            if (nl != null) {
                // looping through all item nodes <item>
                for (int i = 0; i < nl.getLength(); i++) {
                    Element e = (Element) nl.item(i);
                    book.addChapter(e.getTextContent().trim());
                    NodeList nl2 = e.getChildNodes();
                    book.addAnchor("OEBPS/" + parser.getValue((Element) nl2.item(3), "src"));
                }
            }
        }

        return book;
    }

    @Override
    public List<String> getPages(ZipFile epubFile) {
        List<String> pages = new ArrayList<>();

        Enumeration<? extends ZipEntry> entriesList;
        for (entriesList = epubFile.entries(); entriesList.hasMoreElements();) {
            ZipEntry entry = entriesList.nextElement();
            String name = entry.getName();
            if (name.endsWith(".html")) {
                pages.add(name);
            }
        }

        return pages;
    }

    @Override
    public String getHtmlPage(ZipFile epubFile, String htmlFile) throws IOException {
        StringBuilder htmlPageSb = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(epubFile.getInputStream(epubFile.getEntry(htmlFile))), BUFFERSIZE);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            htmlPageSb.append(line);
        }

        return htmlPageSb.toString().replace("<body>", "<body bgcolor=\"Black\"><font color=\"White\">").replace("</body>", "</font></body>");
    }

    @Override
    public void saveImages(ZipFile epubFile, String htmlPage, File imageDirectory) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput(new StringReader(htmlPage.replaceAll("&nbsp;", "")));
            int eventType = xpp.getEventType();

            // Search images in html file
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if(eventType == XmlPullParser.START_TAG && "img".equals(xpp.getName())) {
                    String imageName = xpp.getAttributeValue(null, "src");

                    // Save image
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
                eventType = xpp.next();
            }
        } catch (XmlPullParserException e) {
            System.err.println("XmlPullParserException in image preview");
        } catch (IOException e) {
            System.err.println("IOException in image preview");
        }
    }

    @Override
    public void saveHtmlPage(File htmlFile, String htmlText) throws IOException {
        FileWriter writer = new FileWriter(htmlFile);
        writer.append(htmlText);
        writer.flush();
        writer.close();
    }

    private String getTOC(ZipFile epubFile) throws IOException {
        StringBuilder tocSb = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(epubFile.getInputStream(epubFile.getEntry("OEBPS/toc.ncx"))), BUFFERSIZE);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            tocSb.append(line);
        }
        return tocSb.toString();
    }

}
