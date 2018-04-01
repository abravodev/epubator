package it.iiizio.epubator.presenters;

import java.util.List;

import it.iiizio.epubator.model.entities.FileChooserListItem;

public interface FileChooserPresenter {

    List<FileChooserListItem> getRecentFoldersItems(String history);

    List<FileChooserListItem> getFileItems(String path, String extension, boolean showAllFiles);

    String newHistory(String path, String oldHistory);

}
