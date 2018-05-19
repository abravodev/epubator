package it.iiizio.epubator.presentation.presenters;

public interface MainPresenter {

    String getCoverFileWithTheSameName(String filename);

    boolean showInitialDialog();

    void initialDialogRead();

    void updateRecentFolder(String filename);

    boolean userPrefersToChoosePicture();

    String getRecentFolder();
}
