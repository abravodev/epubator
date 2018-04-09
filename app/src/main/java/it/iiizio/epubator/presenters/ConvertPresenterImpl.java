package it.iiizio.epubator.presenters;

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

import it.iiizio.epubator.model.constants.ConversionStatus;
import it.iiizio.epubator.model.constants.ZipFileConstants;
import it.iiizio.epubator.model.exceptions.ConversionException;
import it.iiizio.epubator.model.utils.HtmlHelper;
import it.iiizio.epubator.model.utils.PdfReadHelper;
import it.iiizio.epubator.model.utils.XMLParser;
import it.iiizio.epubator.model.utils.ZipWriter;
import it.iiizio.epubator.views.activities.ConvertView;
import it.iiizio.epubator.views.utils.BitmapHelper;

public class ConvertPresenterImpl implements ConvertPresenter {

    private final ConvertView view;

    public ConvertPresenterImpl(ConvertView view) {
        this.view = view;
    }

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
        final int maxWidth = 300;
        final int maxHeight = 410;
        final int border = 10;
        final int fontsize = 48;

        // Grey background
        Bitmap bmp = Bitmap.createBitmap(maxWidth, maxHeight, Bitmap.Config.RGB_565);
        Paint paint  = new Paint();
        paint.setColor(Color.LTGRAY);
        Canvas coverImage = new Canvas(bmp);
        coverImage.drawRect(0, 0, maxWidth, maxHeight, paint);

        // Load image
        Bitmap coverImageFile = null;
        if (coverImageFilename != "") {
            // Get dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(coverImageFilename, options);

            // Get image
            options.inSampleSize = BitmapHelper.calculateInSampleSize(options, maxWidth, maxHeight);
            options.inJustDecodeBounds = false;
            coverImageFile = BitmapFactory.decodeFile(coverImageFilename, options);
        }

        if (coverImageFile != null) {
            coverImage.drawBitmap(coverImageFile, null , new Rect(0, 0, maxWidth, maxHeight), new Paint(Paint.FILTER_BITMAP_FLAG));
            view.coverWithImageCreated();
        } else {
            if (showLogoOnCover) {
                addDefaultLogo(maxWidth, maxHeight, coverImage);
            }

            addTitleAsCover(bookFilename, maxWidth, border, fontsize, paint, coverImage);
            view.coverWithTitleCreated();
        }

        boolean error = saveBmpAsPng(bmp);
        if(error){
            throw new ConversionException(ConversionStatus.CANNOT_WRITE_EPUB);
        }
    }

    @Override
    public void addPage(int page, String text) throws ConversionException {
        String htmlText = HtmlHelper.getBasicHtml("page" + page, text.replaceAll("<br/>(?=[a-z])", "&nbsp;"));
        boolean error = ZipWriter.addText(ZipFileConstants.page(page), htmlText, false);
        if(error){
            throw new ConversionException(ConversionStatus.CANNOT_WRITE_EPUB);
        }
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

    private String buildToc(int pages, String tocId, String title, boolean tocFromPdf, int pagesPerFile) {
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
        toc.append("        <text>" + title + "</text>\n");
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
        if (tocFromPdf) {
            // Try to extract toc from pdf
            XMLParser parser = new XMLParser();
            Document doc = parser.getDomElement(PdfReadHelper.getBookmarks());
            if (doc != null) {
                doc.normalize();
                NodeList nl = doc.getElementsByTagName("Title");
                if (nl != null) {
                    int lastPage = Integer.MAX_VALUE;
                    StringBuilder sb = new StringBuilder();
                    // looping through all item nodes <item>
                    for (int i = 0; i < nl.getLength(); i++) {
                        Element e = (Element) nl.item(i);
                        String action = parser.getValue(e, "Action");
                        if (action.equals("GoTo")) {
                            String chapter = parser.getElementValue(e).trim();
                            try {
                                int page = Integer.parseInt(parser.getValue(e, "Page").split(" ")[0]);

                                // First entry not in page one, create a dummy one
                                if ((lastPage == Integer.MAX_VALUE) && (page > 1))
                                {
                                    sb.append(title);
                                    sb.append("\n");
                                    lastPage = 1;
                                }

                                // Add entry in toc
                                if (page > lastPage) {
                                    String entry = buildXmlEntry(pagesPerFile, lastPage, playOrder, sb);
                                    toc.append(entry);
                                    playOrder += 1;

                                    sb = new StringBuilder();
                                }

                                // Set next entry
                                sb.append(chapter);
                                sb.append("\n");
                                lastPage = page;
                            } catch (RuntimeException ex) {
                                System.err.println("RuntimeException in xml extraction " + ex.getMessage());
                            }
                        }
                        extractedToc = true;
                    }

                    // Add last entry
                    if (sb.length() > 0) {
                        String entry = buildXmlEntry(pagesPerFile, lastPage, playOrder, sb);
                        toc.append(entry);
                    }
                }
            }
        }

        if(extractedToc){
            view.tocExtractedFromPdfFile();
        } else {
            if(tocFromPdf) {
                view.noTocFoundInThePdfFile();
            }
            view.createdDummyToc();

            for(int i = 1; i <= pages; i += pagesPerFile) {
                toc.append("        <navPoint id=\"navPoint-" + playOrder + "\" playOrder=\"" + playOrder + "\">\n");
                toc.append("            <navLabel>\n");
                toc.append("                <text>Page" + i + "</text>\n");
                toc.append("            </navLabel>\n");
                toc.append("            <content src=\"page" + i + ".html\"/>\n");
                toc.append("        </navPoint>\n");
                playOrder += 1;
            }
        }

        toc.append("    </navMap>\n");
        toc.append("</ncx>\n");
        return toc.toString();
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
        StringBuilder toc  = new StringBuilder();

        int pageFile = pageFile(pagesPerFile, lastPage);
        toc.append("        <navPoint id=\"navPoint-" + playOrder + "\" playOrder=\"" + playOrder + "\">\n");
        toc.append("            <navLabel>\n");
        toc.append("                <text>" + sb.toString() + "                </text>\n");
        toc.append("            </navLabel>\n");
        toc.append("            <content src=\"page" + pageFile + ".html#page" + lastPage + "\"/>\n");
        toc.append("        </navPoint>\n");

        return toc.toString();
    }

    private int pageFile(int pagesPerFile, int lastPage){
        return ((lastPage - 1) / pagesPerFile) * pagesPerFile + 1;
    }

    private void addDefaultLogo(int maxWidth, int maxHeight, Canvas coverImage) {
        Bitmap coverImageFile = view.getAppLogo();
        coverImage.drawBitmap(coverImageFile, maxWidth - coverImageFile.getWidth(), maxHeight - coverImageFile.getHeight(), new Paint(Paint.FILTER_BITMAP_FLAG));
    }

    private boolean saveBmpAsPng(Bitmap bmp) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return ZipWriter.addImage(ZipFileConstants.FRONTPAGE_IMAGE, outputStream.toByteArray());
    }

    private void addTitleAsCover(String bookFilename, int maxWidth, int border, int fontsize, Paint paint, Canvas coverImage) {
        paint.setTextSize(fontsize);
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);

        String[] words = getTitleWords(bookFilename);

        float newline = paint.getFontSpacing();
        float x = border;
        float y = newline;

        for (String word : words) {
            float len = paint.measureText(word + " ");

            // Line wrap
            if ((x > border) && ((x + len) > maxWidth)) {
                x = border;
                y += newline;
            }

            // Word wrap
            while ((x + len) > maxWidth) {
                int maxChar = (int) (word.length() * (maxWidth - border - x) / paint.measureText(word));
                coverImage.drawText(word.substring(0, maxChar), x, y, paint);
                word = word.substring(maxChar);
                len = paint.measureText(word + " ");
                x = border;
                y += newline;
            }

            coverImage.drawText(word, x, y, paint);
            x += len;
        }
    }

    private String[] getTitleWords(String bookFilename){
        String title = bookFilename.replaceAll("_", " ");
        return title.split("\\s");
    }

}
