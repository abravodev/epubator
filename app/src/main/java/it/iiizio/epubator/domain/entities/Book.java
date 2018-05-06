package it.iiizio.epubator.domain.entities;

import java.util.ArrayList;
import java.util.List;

public class Book {

    private final List<String> chapters;
    private final List<String> anchors;
    private final List<String> pages;

    public Book(List<String> pages) {
        this.pages = pages;
        chapters = new ArrayList<>();
        anchors = new ArrayList<>();
    }

    public void addChapter(String chapter){
        chapters.add(chapter);
    }

    public void addAnchor(String anchor){
        anchors.add(anchor);
    }

    public CharSequence[] getChapters(){
        return chapters.toArray(new CharSequence[chapters.size()]);
    }

    public String getAnchor(int anchorIndex){
        String[] links = getAnchorLinks(anchorIndex);
        return links.length > 1 ? links[1] : null;
    }

    public int getPagesCount(){
        return pages.size();
    }

    public String getPage(int index){
    	return pages.get(index - 1);
    }

    public int getPageIndex(int anchorIndex){
        String[] links = getAnchorLinks(anchorIndex);
        return pages.indexOf(links[0]);
    }

    public boolean hasPreviousPage(int currentPageIndex){
    	return currentPageIndex>1;
	}

	public boolean hasNextPage(int currentPageIndex){
    	return currentPageIndex < getPagesCount();
	}

	public String getAnchorFromPageName(int pageIndex){
		String fileName = getPage(pageIndex);
		return fileName.substring(fileName.lastIndexOf("/") + 1, fileName.lastIndexOf("."));
	}

	public boolean isValidPage(int page){
		return 1 <= page && page <= getPagesCount();
	}

	private String[] getAnchorLinks(int index){
		return anchors.get(index).split("#");
	}
}
