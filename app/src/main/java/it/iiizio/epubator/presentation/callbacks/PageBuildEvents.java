package it.iiizio.epubator.presentation.callbacks;

import android.graphics.Bitmap;

public interface PageBuildEvents {

    void addPage(int page);

    void pageFailure(int j);

    void imageAdded(String imageName);

    void noTocFoundInThePdfFile();

    void createdDummyToc();

    void tocExtractedFromPdfFile();

    void coverWithImageCreated();

    void coverWithTitleCreated();

    Bitmap getAppLogo();

}
