package it.iiizio.epubator.domain.entities;

import java.util.ArrayList;
import java.util.List;

public class Book {

    private List<String> chapters;
    private List<String> anchors;

    public Book() {
        chapters = new ArrayList<>();
        anchors = new ArrayList<>();
    }

    public void addChapter(String chapter){
        chapters.add(chapter);
    }

    public void addAnchor(String anchor){
        anchors.add(anchor);
    }

    public List<String> getChapters(){
        return chapters;
    }

    public List<String> getAnchors(){
        return anchors;
    }

}
