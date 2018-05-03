package it.iiizio.epubator.domain.callbacks;

public interface PageBuildEvents {

    void pageAdded(int page);

    void pageFailure(int j);

    void imageAdded(String imageName);

    void noTocFoundInThePdfFile();

    void dummyTocCreated();

    void tocExtractedFromPdfFile();

    void coverWithImageCreated();

    void coverWithTitleCreated();

    String getLocaleLanguage();
}
