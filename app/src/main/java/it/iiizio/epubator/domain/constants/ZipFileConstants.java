package it.iiizio.epubator.domain.constants;

public class ZipFileConstants {

    public static final String MIMETYPE = "mimetype";
    public static final String FRONTPAGE = "OEBPS/frontpage.html";
    public static final String FRONTPAGE_IMAGE = "OEBPS/frontpage.png";
    public static final String CONTENT = "OEBPS/content.opf";
    public static final String CONTAINER = "META-INF/container.xml";
    public static final String TOC = "OEBPS/toc.ncx";

    public static String page(int page){
        return "OEBPS/page" + page + ".html";
    }

    public static String getPage(String pageName){
    	int startPageChar = pageName.lastIndexOf("/")+1;
    	int endPageChar = pageName.lastIndexOf(".");
    	return pageName.substring(startPageChar, endPageChar);
	}

    public static String image(String imageName){
    	return "OEBPS/" + imageName;
	}

	public static String anchor(String anchor){
    	return "OEBPS/" + anchor;
	}
}
