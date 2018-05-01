package it.iiizio.epubator.infrastructure.services;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.ByteArrayOutputStream;
import java.io.File;

import it.iiizio.epubator.domain.constants.ConversionStatus;
import it.iiizio.epubator.domain.constants.ZipFileConstants;
import it.iiizio.epubator.domain.entities.Chapter;
import it.iiizio.epubator.domain.entities.FrontCoverDetails;
import it.iiizio.epubator.domain.exceptions.ConversionException;
import it.iiizio.epubator.domain.utils.HtmlHelper;
import it.iiizio.epubator.domain.utils.PdfReadHelper;
import it.iiizio.epubator.domain.utils.XMLParser;
import it.iiizio.epubator.domain.utils.ZipWriter;
import it.iiizio.epubator.presentation.callbacks.PageBuildEvents;
import it.iiizio.epubator.presentation.dto.ConversionPreferences;
import it.iiizio.epubator.presentation.dto.ConversionSettings;
import it.iiizio.epubator.presentation.dto.PdfExtraction;
import it.iiizio.epubator.presentation.utils.BitmapHelper;

public class ConvertManagerImpl implements ConvertManager {

    //<editor-fold desc="Attributes">
    private final PageBuildEvents pageBuildEvents;
    //</editor-fold>

    //<editor-fold desc="Constructors">
    public ConvertManagerImpl(PageBuildEvents buildEvents) {
        this.pageBuildEvents = buildEvents;
    }
    //</editor-fold>

    //<editor-fold desc="Public methods">
    @Override
    public void loadPdfFile(String pdfFilename) throws ConversionException {
        if (!(new File(pdfFilename).exists())) {
            throw new ConversionException(ConversionStatus.FILE_NOT_FOUND);
        }

        boolean error = PdfReadHelper.open(pdfFilename);
        if (error) {
            throw new ConversionException(ConversionStatus.CANNOT_READ_PDF);
        }
    }

    @Override
    public void openFile(String tempFilename) throws ConversionException {
        boolean error = ZipWriter.create(tempFilename);
        if(error){
            throw new ConversionException(ConversionStatus.CANNOT_WRITE_EPUB);
        }
    }

    @Override
    public void addMimeType() throws ConversionException {
        String filetype = "application/epub+zip";
        boolean error = ZipWriter.addText(ZipFileConstants.MIMETYPE, filetype, true);
        if(error){
            throw new ConversionException(ConversionStatus.CANNOT_WRITE_EPUB);
        }
    }

    @Override
    public void addContainer() throws ConversionException {
        String container = buildContainer();
        boolean error = ZipWriter.addText("META-INF/container.xml", container, false);
        if(error){
           throw new ConversionException(ConversionStatus.CANNOT_WRITE_EPUB);
        }
    }

    @Override
    public void addToc(int pages, String tocId, String title, boolean tocFromPdf, int pagesPerFile) throws ConversionException {
        String toc = buildToc(pages, tocId, title, tocFromPdf, pagesPerFile);
        boolean error = ZipWriter.addText("OEBPS/toc.ncx", toc, false);
        if(error){
            throw new ConversionException(ConversionStatus.CANNOT_WRITE_EPUB);
        }
    }

    @Override
    public void addFrontPage() throws ConversionException {
        String frontPage = buildFrontPage();
        boolean error = ZipWriter.addText(ZipFileConstants.FRONTPAGE, frontPage, false);
        if(error){
            throw new ConversionException(ConversionStatus.CANNOT_WRITE_EPUB);
        }
    }

    @Override
    public void addFrontpageCover(String bookFilename, String coverImageFilename, boolean showLogoOnCover) throws ConversionException {
        FrontCoverDetails coverDetails = new FrontCoverDetails();

        Bitmap bitmap = Bitmap.createBitmap(coverDetails.getWidth(), coverDetails.getHeight(), Bitmap.Config.RGB_565);
        Paint paint = new Paint();
        paint.setColor(Color.LTGRAY);
        Canvas coverImage = new Canvas(bitmap);
        coverImage.drawRect(0, 0, coverDetails.getWidth(), coverDetails.getHeight(), paint);

        boolean coverImageAdded = addSelectedCoverImage(coverImageFilename, coverImage, coverDetails);
        if(!coverImageAdded) {
            addDefaultCover(bookFilename, showLogoOnCover, coverDetails, paint, coverImage);
        }

        boolean error = saveBitmapAsPng(bitmap);
        if(error){
            throw new ConversionException(ConversionStatus.CANNOT_WRITE_EPUB);
        }
    }

    @Override
    public PdfExtraction addPages(ConversionPreferences preferences, int pages, int pagesPerFile) throws ConversionException {
        PdfExtraction extraction = new PdfExtraction(preferences, pages, pagesPerFile, pageBuildEvents);

        for(int i = 1; i <= pages; i += pagesPerFile) {
            String pageText = extraction.buildPage(i);
            addPage(i, pageText);
        }

        return extraction;
    }

    @Override
    public void addContent(int pages, String bookId, String title, Iterable<String> images, int pagesPerFile) throws ConversionException {
        String content = buildContent(pages, bookId, images, title, pagesPerFile);
        boolean error = ZipWriter.addText(ZipFileConstants.CONTENT, content, false);
        if(error){
            throw new ConversionException(ConversionStatus.CANNOT_WRITE_EPUB);
        }
    }

    @Override
    public void closeFile(String tempFilename) throws ConversionException {
        boolean error = ZipWriter.close();
        if(error){
            throw new ConversionException(ConversionStatus.CANNOT_WRITE_EPUB);
        }
    }

    @Override
    public void saveEpub(ConversionSettings settings) {
        new File(settings.tempFilename).renameTo(new File(settings.epubFilename));
        new File(settings.oldFilename).delete();
    }

    @Override
    public boolean deleteTemporalFile(ConversionSettings settings) {
        new File(settings.tempFilename).delete();
        if (new File(settings.oldFilename).exists()) {
            new File(settings.oldFilename).renameTo(new File(settings.epubFilename));
            return true;
        }
        return false;
    }
    //</editor-fold>

    //<editor-fold desc="Private methods">
    private BitmapFactory.Options getBitmapOptions(String coverImageFilename, int maxWidth, int maxHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(coverImageFilename, options);

        // Get image
        options.inSampleSize = BitmapHelper.calculateInSampleSize(options, maxWidth, maxHeight);
        options.inJustDecodeBounds = false;
        return options;
    }

    private void addPage(int page, String text) throws ConversionException {
        String htmlText = HtmlHelper.getBasicHtml("page" + page, text.replaceAll("<br/>(?=[a-z])", "&nbsp;"));
        boolean error = ZipWriter.addText(ZipFileConstants.page(page), htmlText, false);
        if(error){
            throw new ConversionException(ConversionStatus.CANNOT_WRITE_EPUB);
        }
    }

    private String buildToc(int pages, String tocId, String bookTitle, boolean getTocFromPdf, int pagesPerFile) {
        StringBuilder toc = new StringBuilder();
        toc.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        toc.append("<!DOCTYPE ncx PUBLIC \"-//NISO//DTD ncx 2005-1//EN\"\n");
        toc.append("   \"http://www.daisy.org/z3986/2005/ncx-2005-1.dtd\">\n");
        toc.append("<ncx xmlns=\"http://www.daisy.org/z3986/2005/ncx/\" version=\"2005-1\">\n");
        toc.append("    <head>\n");
        toc.append("        <meta name=\"dtb:uid\" content=\"" + tocId + "\"/>\n");
        toc.append("        <meta name=\"dtb:depth\" content=\"1\"/>\n");
        toc.append("        <meta name=\"dtb:totalPageCount\" content=\"0\"/>\n");
        toc.append("        <meta name=\"dtb:maxPageNumber\" content=\"0\"/>\n");
        toc.append("    </head>\n");
        toc.append("    <docTitle>\n");
        toc.append("        <text>" + bookTitle + "</text>\n");
        toc.append("    </docTitle>\n");
        toc.append("    <navMap>\n");
        toc.append("        <navPoint id=\"navPoint-1\" playOrder=\"1\">\n");
        toc.append("            <navLabel>\n");
        toc.append("                <text>Frontpage</text>\n");
        toc.append("            </navLabel>\n");
        toc.append("            <content src=\"frontpage.html\"/>\n");
        toc.append("        </navPoint>\n");

        int playOrder = 2;
        boolean extractedToc = false;
        if (getTocFromPdf) {
            XMLParser parser = new XMLParser();
            NodeList nodes = getNodes(parser, PdfReadHelper.getBookmarks());
            if (nodes != null) {
                extractedToc = nodes.getLength()>0;
                String tocFromPdf = buildTocFromPdf(nodes, parser, bookTitle, pagesPerFile, playOrder);
                toc.append(tocFromPdf);
            }
        }

        if(extractedToc){
            pageBuildEvents.tocExtractedFromPdfFile();
        } else {
            if(getTocFromPdf) {
                pageBuildEvents.noTocFoundInThePdfFile();
            }
            String dummyToc = buildDummyToc(pages, pagesPerFile, playOrder);
            toc.append(dummyToc);
            pageBuildEvents.dummyTocCreated();
        }

        toc.append("    </navMap>\n");
        toc.append("</ncx>\n");
        return toc.toString();
    }

    private String buildTocFromPdf(NodeList nodes, XMLParser parser, String bookTitle, int pagesPerFile, int playOrder){
        int lastPage = Integer.MAX_VALUE;
        StringBuilder toc = new StringBuilder();
        StringBuilder sb = new StringBuilder();

        // looping through all item nodes <item>
        for (int i = 0; i < nodes.getLength(); i++) {
            try {
				Element chapterElement = (Element) nodes.item(i);
				Chapter chapter = Chapter.make(parser, chapterElement);
				if(chapter==null){
					continue;
				}

				// First entry not in page one, create a dummy one
                if ((lastPage == Integer.MAX_VALUE) && (chapter.getPageIndex() > 1)) {
                    sb.append(bookTitle).append("\n");
                    lastPage = 1;
                }

                // Add entry in toc
                if (chapter.getPageIndex() > lastPage) {
                    String entry = buildXmlEntry(pagesPerFile, lastPage, playOrder, sb);
                    toc.append(entry);
                    playOrder++;

                    sb = new StringBuilder();
                }

                // Set next entry
                sb.append(chapter.getTitle());
                sb.append("\n");
                lastPage = chapter.getPageIndex();
            } catch (RuntimeException ex) {
                System.err.println("RuntimeException in xml extraction " + ex.getMessage());
            }

        }

        // Add last entry
        if (sb.length() > 0) {
            String entry = buildXmlEntry(pagesPerFile, lastPage, playOrder, sb);
            toc.append(entry);
        }

        return toc.toString();
    }

    private String buildDummyToc(int pages, int pagesPerFile, int playOrder){
        StringBuilder toc = new StringBuilder();
        for(int i = 1; i <= pages; i += pagesPerFile) {
            String entryText = String.format("Page %d", i);
            String entryContentSource = String.format("page%d.html", i);
            String entry = buildXmlEntry(playOrder, entryText, entryContentSource);
            toc.append(entry);
            playOrder++;
        }
        return toc.toString();
    }

    private NodeList getNodes(XMLParser parser, String bookmarks){
        Document doc = parser.getDomElement(bookmarks);
        if (doc == null) {
            return null;
        }

        doc.normalize();
        return doc.getElementsByTagName("Title");
    }

    private String buildContainer() {
        StringBuilder container = new StringBuilder();
        container.append("<?xml version=\"1.0\"?>\n");
        container.append("<container version=\"1.0\" xmlns=\"urn:oasis:names:tc:opendocument:xmlns:container\">\n");
        container.append("   <rootfiles>\n");
        container.append("      <rootfile full-path=\"OEBPS/content.opf\" media-type=\"application/oebps-package+xml\"/>\n");
        container.append("   </rootfiles>\n");
        container.append("</container>\n");
        return container.toString();
    }

    private String buildFrontPage() {
        return HtmlHelper.getBasicHtml("Frontpage", "<div><img width=\"100%\" alt=\"cover\" src=\"frontpage.png\" /></div>");
    }

    private String buildContent(int pages, String id, Iterable<String> images, String title, int pagesPerFile) {
        StringBuilder content = new StringBuilder();

        content.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        content.append("<package xmlns=\"http://www.idpf.org/2007/opf\" unique-identifier=\"BookID\" version=\"2.0\">\n");
        content.append("    <metadata xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:opf=\"http://www.idpf.org/2007/opf\">\n");
        content.append("        <dc:title>" + title + "</dc:title>\n");
        content.append("        <dc:creator>" + (PdfReadHelper.getAuthor()).replaceAll("[<>]", "_") + "</dc:creator>\n");
        content.append("        <dc:creator opf:role=\"bkp\">ePUBator - Minimal offline PDF to ePUB converter for Android</dc:creator>\n");
        content.append("        <dc:identifier id=\"BookID\" opf:scheme=\"UUID\">" + id + "</dc:identifier>\n");
        content.append("        <dc:language>" + Resources.getSystem().getConfiguration().locale.getLanguage() + "</dc:language>\n");
        content.append("    </metadata>\n");
        content.append("    <manifest>\n");

        for(int i = 1; i <= pages; i += pagesPerFile) {
            content.append("        <item id=\"page" + i + "\" href=\"page" + i + ".html\" media-type=\"application/xhtml+xml\"/>\n");
        }
        content.append("        <item id=\"ncx\" href=\"toc.ncx\" media-type=\"application/x-dtbncx+xml\"/>\n");
        content.append("        <item id=\"frontpage\" href=\"frontpage.html\" media-type=\"application/xhtml+xml\"/>\n");
        content.append("        <item id=\"cover\" href=\"frontpage.png\" media-type=\"image/png\"/>\n");

        for(String name : images) {
            content.append("        <item id=\"" + name + "\" href=\"" + name + "\" media-type=\"image/" + name.substring(name.lastIndexOf('.') + 1) + "\"/>\n");
        }

        content.append("    </manifest>\n");
        content.append("    <spine toc=\"ncx\">\n");
        content.append("        <itemref idref=\"frontpage\"/>\n");

        for(int i = 1; i <= pages; i += pagesPerFile) {
            content.append("        <itemref idref=\"page" + i + "\"/>\n");
        }

        content.append("    </spine>\n");
        content.append("    <guide>\n");
        content.append("        <reference type=\"cover\" title=\"Frontpage\" href=\"frontpage.html\"/>\n");
        content.append("    </guide>\n");
        content.append("</package>\n");

        return content.toString();
    }

    private String buildXmlEntry(int pagesPerFile, int lastPage, int playOrder, StringBuilder sb){
        int pageFile = getPageFile(pagesPerFile, lastPage);
        String contentSource = String.format("page%d.html#page%d", pageFile, lastPage);
        return buildXmlEntry(playOrder, sb.toString(), contentSource);
    }

    private String buildXmlEntry(int playOrder, String text, String contentSource){
        StringBuilder toc = new StringBuilder();

        toc.append("        <navPoint id=\"navPoint-" + playOrder + "\" playOrder=\"" + playOrder + "\">\n");
        toc.append("            <navLabel>\n");
        toc.append("                <text>" + text + "                </text>\n");
        toc.append("            </navLabel>\n");
        toc.append("            <content src=\"" + contentSource + "\"/>\n");
        toc.append("        </navPoint>\n");

        return toc.toString();
    }

    private int getPageFile(int pagesPerFile, int lastPage){
        return ((lastPage - 1) / pagesPerFile) * pagesPerFile + 1;
    }

    private boolean addSelectedCoverImage(String coverImageFilename, Canvas coverImage, FrontCoverDetails coverDetails){
        if(coverImageFilename.equals("")){
            return false;
        }

        final BitmapFactory.Options options = getBitmapOptions(coverImageFilename, coverDetails.getWidth(), coverDetails.getHeight());
        Bitmap coverImageFile = BitmapFactory.decodeFile(coverImageFilename, options);

        if(coverImageFile == null){
            return false;
        }

        coverImage.drawBitmap(coverImageFile,
                null,
                new Rect(0, 0, coverDetails.getWidth(), coverDetails.getHeight()),
                new Paint(Paint.FILTER_BITMAP_FLAG));
        pageBuildEvents.coverWithImageCreated();

        return true;
    }

    private void addDefaultCover(String bookFilename, boolean showLogoOnCover, FrontCoverDetails coverDetails, Paint paint, Canvas coverImage) {
        if (showLogoOnCover) {
            paintLogoOnTheCover(coverDetails, coverImage);
        }

        paintTitleOnTheCover(bookFilename, coverDetails, paint, coverImage);
        pageBuildEvents.coverWithTitleCreated();
    }

    private void paintLogoOnTheCover(FrontCoverDetails coverDetails, Canvas coverImage) {
        Bitmap coverImageFile = pageBuildEvents.getAppLogo();
        coverImage.drawBitmap(coverImageFile,
                coverDetails.getWidth() - coverImageFile.getWidth(),
                coverDetails.getHeight()- coverImageFile.getHeight(),
                new Paint(Paint.FILTER_BITMAP_FLAG));
    }

    private boolean saveBitmapAsPng(Bitmap bmp) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return ZipWriter.addImage(ZipFileConstants.FRONTPAGE_IMAGE, outputStream.toByteArray());
    }

    private void paintTitleOnTheCover(String bookFilename, FrontCoverDetails coverDetails, Paint paint, Canvas coverImage) {
        paint.setTextSize(coverDetails.getFontSize());
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);

        String[] words = getTitleWords(bookFilename);

        float newline = paint.getFontSpacing();
        float x = coverDetails.getBorder();
        float y = newline;

        for (String word: words) {
            float wordLength = paint.measureText(word + " ");

            // Line wrap
            if ((x > coverDetails.getBorder()) && ((x + wordLength) > coverDetails.getWidth())) {
                x = coverDetails.getBorder();
                y += newline;
            }

            // Word wrap
            while ((x + wordLength) > coverDetails.getWidth()) {
                int maxChar = (int) (word.length() * (coverDetails.getWidth() - coverDetails.getBorder() - x) / paint.measureText(word));
                coverImage.drawText(word.substring(0, maxChar), x, y, paint);
                word = word.substring(maxChar);
                wordLength = paint.measureText(word + " ");
                x = coverDetails.getBorder();
                y += newline;
            }

            coverImage.drawText(word, x, y, paint);
            x += wordLength;
        }
    }

    private String[] getTitleWords(String bookFilename){
        String title = bookFilename.replaceAll("_", " ");
        return title.split("\\s");
    }
    //</editor-fold>
}