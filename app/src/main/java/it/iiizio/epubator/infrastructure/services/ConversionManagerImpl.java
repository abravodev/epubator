package it.iiizio.epubator.infrastructure.services;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import it.iiizio.epubator.domain.callbacks.PageBuildEvents;
import it.iiizio.epubator.domain.constants.ConversionStatus;
import it.iiizio.epubator.domain.constants.FileTypes;
import it.iiizio.epubator.domain.constants.ZipFileConstants;
import it.iiizio.epubator.domain.entities.Chapter;
import it.iiizio.epubator.domain.entities.ConversionPreferences;
import it.iiizio.epubator.domain.entities.ConversionSettings;
import it.iiizio.epubator.domain.entities.FrontCoverDetails;
import it.iiizio.epubator.domain.entities.PdfExtraction;
import it.iiizio.epubator.domain.exceptions.ConversionException;
import it.iiizio.epubator.domain.services.PdfReaderService;
import it.iiizio.epubator.domain.services.ZipWriterService;
import it.iiizio.epubator.domain.utils.HtmlHelper;
import it.iiizio.epubator.domain.utils.PdfXmlParser;
import it.iiizio.epubator.infrastructure.providers.FileProvider;
import it.iiizio.epubator.infrastructure.providers.ImageProvider;

public class ConversionManagerImpl implements ConversionManager {

    //<editor-fold desc="Attributes">
    private final PageBuildEvents pageBuildEvents;
    private final ImageProvider imageProvider;
    private final PdfReaderService pdfReader;
    private final ZipWriterService zipWriter;
    private final FileProvider storageProvider;
	private final PdfXmlParser parser;
    //</editor-fold>

    //<editor-fold desc="Constructors">
    public ConversionManagerImpl(PageBuildEvents buildEvents, ImageProvider imageProvider,
			FileProvider storageProvider, PdfReaderService pdfReader, ZipWriterService zipWriter, PdfXmlParser parser) {
        this.pageBuildEvents = buildEvents;
        this.imageProvider = imageProvider;
        this.storageProvider = storageProvider;
        this.pdfReader = pdfReader;
		this.zipWriter = zipWriter;
		this.parser = parser;
	}
    //</editor-fold>

	//<editor-fold desc="Public methods">
    @Override
    public void loadPdfFile(String pdfFilename) throws ConversionException {
        if (!storageProvider.exists(pdfFilename)) {
            throw new ConversionException(ConversionStatus.FILE_NOT_FOUND);
        }

        boolean error = pdfReader.open(pdfFilename);
        if (error) {
            throw new ConversionException(ConversionStatus.CANNOT_READ_PDF);
        }
    }

    @Override
    public void openFile(String tempFilename) throws ConversionException {
        boolean error = zipWriter.create(tempFilename);
        if(error){
            throw new ConversionException(ConversionStatus.CANNOT_WRITE_EPUB);
        }
    }

	@Override
	public int getBookPages() {
		return pdfReader.getPages();
	}

    @Override
    public void addMimeType() throws ConversionException {
        boolean error = zipWriter.addText(ZipFileConstants.MIMETYPE, FileTypes.EPUB, true);
        if(error){
            throw new ConversionException(ConversionStatus.CANNOT_WRITE_EPUB);
        }
    }

    @Override
    public void addContainer() throws ConversionException {
        String container = buildContainer();
        boolean error = zipWriter.addText(ZipFileConstants.CONTAINER, container);
        if(error){
           throw new ConversionException(ConversionStatus.CANNOT_WRITE_EPUB);
        }
    }

    @Override
    public void addToc(String tocId, String title, boolean tocFromPdf, int pagesPerFile) throws ConversionException {
        String toc = buildToc(tocId, title, tocFromPdf, pagesPerFile);
        boolean error = zipWriter.addText(ZipFileConstants.TOC, toc);
        if(error){
            throw new ConversionException(ConversionStatus.CANNOT_WRITE_EPUB);
        }
    }

    @Override
    public void addFrontPage() throws ConversionException {
        String frontPage = buildFrontPage();
        boolean error = zipWriter.addText(ZipFileConstants.FRONTPAGE, frontPage);
        if(error){
            throw new ConversionException(ConversionStatus.CANNOT_WRITE_EPUB);
        }
    }

    @Override
    public void addFrontpageCover(String bookFilename, String coverImageFilename, boolean showLogoOnCover) throws ConversionException {
        FrontCoverDetails coverDetails = new FrontCoverDetails();

        byte[] coverImage = imageProvider.addSelectedCoverImage(coverImageFilename, coverDetails);
        if(coverImage!=null){
			pageBuildEvents.coverWithImageCreated();
			saveBitmapAsPng(coverImage);
		} else {
			String[] titleWords = getTitleWords(bookFilename);
			coverImage = imageProvider.addDefaultCover(titleWords, showLogoOnCover, coverDetails);
			pageBuildEvents.coverWithTitleCreated();
			saveBitmapAsPng(coverImage);
        }
    }

    @Override
    public PdfExtraction addPages(ConversionPreferences preferences) throws ConversionException {
    	PdfExtraction extraction = new PdfExtraction(preferences, pageBuildEvents, pdfReader, zipWriter);

		int pages = extraction.getPages();
		for(int i = 1; i <= pages; i += preferences.pagesPerFile) {
			pageBuildEvents.pageAdded(i);
            String pageText = extraction.buildPage(i);
            addPage(i, pageText);
        }

        return extraction;
    }

    @Override
    public void addContent(String bookId, String title, Iterable<String> images, int pagesPerFile) throws ConversionException {
        String content = buildContent(bookId, images, title, pagesPerFile);
        boolean error = zipWriter.addText(ZipFileConstants.CONTENT, content);
        if(error){
            throw new ConversionException(ConversionStatus.CANNOT_WRITE_EPUB);
        }
    }

    @Override
    public void closeFile(String tempFilename) throws ConversionException {
        boolean error = zipWriter.close();
        if(error){
            throw new ConversionException(ConversionStatus.CANNOT_WRITE_EPUB);
        }
    }

    @Override
    public void saveEpub(ConversionSettings settings) {
        storageProvider.rename(settings.tempFilename, settings.epubFilename);
        storageProvider.remove(settings.oldFilename);
    }

    @Override
    public boolean deleteTemporalFile(ConversionSettings settings) {
        storageProvider.remove(settings.tempFilename);
        if (storageProvider.exists(settings.oldFilename)) {
            return storageProvider.rename(settings.oldFilename, settings.epubFilename);
        }
        return false;
    }

	@Override
	public void saveOldEpub(ConversionSettings settings) {
		if (storageProvider.exists(settings.epubFilename)) {
			storageProvider.rename(settings.epubFilename, settings.oldFilename);
		}
	}

	@Override
	public void removeCacheFiles(ConversionSettings settings) {
		storageProvider.removeAllFromDirectory(settings.temporalPath);
	}
	//</editor-fold>

    //<editor-fold desc="Private methods">
    private void addPage(int page, String text) throws ConversionException {
        String htmlText = HtmlHelper.getBasicHtml("page" + page, text.replaceAll("<br/>(?=[a-z])", "&nbsp;"));
        boolean error = zipWriter.addText(ZipFileConstants.page(page), htmlText);
        if(error){
            throw new ConversionException(ConversionStatus.CANNOT_WRITE_EPUB);
        }
    }

    private String buildToc(String tocId, String bookTitle, boolean getTocFromPdf, int pagesPerFile) {
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
            NodeList nodes = parser.getDocumentTitles(pdfReader.getBookmarks());
            if (nodes != null) {
                extractedToc = nodes.getLength()>0;
                String tocFromPdf = buildTocFromPdf(nodes, bookTitle, pagesPerFile, playOrder);
                toc.append(tocFromPdf);
            }
        }

        if(extractedToc){
            pageBuildEvents.tocExtractedFromPdfFile();
        } else {
            if(getTocFromPdf) {
                pageBuildEvents.noTocFoundInThePdfFile();
            }
            String dummyToc = buildDummyToc(pagesPerFile, playOrder);
            toc.append(dummyToc);
            pageBuildEvents.dummyTocCreated();
        }

        toc.append("    </navMap>\n");
        toc.append("</ncx>\n");
        return toc.toString();
    }

    private String buildTocFromPdf(NodeList nodes, String bookTitle, int pagesPerFile, int playOrder){
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

    private String buildDummyToc(int pagesPerFile, int playOrder){
		int pages = getBookPages();
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

    private String buildContent(String id, Iterable<String> images, String title, int pagesPerFile) {
        StringBuilder content = new StringBuilder();

		int pages = getBookPages();

        String author = (pdfReader.getAuthor()).replaceAll("[<>]", "_");
        content.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        content.append("<package xmlns=\"http://www.idpf.org/2007/opf\" unique-identifier=\"BookID\" version=\"2.0\">\n");
        content.append("    <metadata xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:opf=\"http://www.idpf.org/2007/opf\">\n");
        content.append("        <dc:title>" + title + "</dc:title>\n");
        content.append("        <dc:creator>" + author + "</dc:creator>\n");
        content.append("        <dc:creator opf:role=\"bkp\">ePUBator - Minimal offline PDF to ePUB converter for Android</dc:creator>\n");
        content.append("        <dc:identifier id=\"BookID\" opf:scheme=\"UUID\">" + id + "</dc:identifier>\n");
        content.append("        <dc:language>" + pageBuildEvents.getLocaleLanguage() + "</dc:language>\n");
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
        return new StringBuilder()
			.append("        <navPoint id=\"navPoint-" + playOrder + "\" playOrder=\"" + playOrder + "\">\n")
			.append("            <navLabel>\n")
			.append("                <text>" + text + "                </text>\n")
			.append("            </navLabel>\n")
			.append("            <content src=\"" + contentSource + "\"/>\n")
			.append("        </navPoint>\n")
			.toString();
    }

    private int getPageFile(int pagesPerFile, int lastPage){
        return ((lastPage - 1) / pagesPerFile) * pagesPerFile + 1;
    }

    private void saveBitmapAsPng(byte[] outputStream) throws ConversionException {
        boolean error = zipWriter.addImage(ZipFileConstants.FRONTPAGE_IMAGE, outputStream);
		if(error){
			throw new ConversionException(ConversionStatus.CANNOT_WRITE_EPUB);
		}
    }

    private String[] getTitleWords(String bookFilename){
        String title = bookFilename.replaceAll("_", " ");
        return title.split("\\s");
    }
    //</editor-fold>
}