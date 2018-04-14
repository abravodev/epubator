package it.iiizio.epubator.presentation.views.activities;

import android.graphics.Bitmap;

public interface ConvertView {

    void noTocFoundInThePdfFile();

    void createdDummyToc();

    void tocExtractedFromPdfFile();

    void coverWithImageCreated();

    void coverWithTitleCreated();

    Bitmap getAppLogo();

}
