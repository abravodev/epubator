package it.iiizio.epubator.presentation.callbacks;

public interface PageBuild {

    void addPage(int page);

    void pageFailure(int j);

    void imageAdded(String imageName);

}
