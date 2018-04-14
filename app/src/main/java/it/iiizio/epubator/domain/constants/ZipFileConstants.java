package it.iiizio.epubator.domain.constants;

public class ZipFileConstants {

    public static final String MIMETYPE = "mimetype";
    public static final String FRONTPAGE = "OEBPS/frontpage.html";
    public static final String FRONTPAGE_IMAGE = "OEBPS/frontpage.png";
    public static final String CONTENT = "OEBPS/content.opf";

    public static String page(int page){
        return "OEBPS/page" + page + ".html";
    }
}
