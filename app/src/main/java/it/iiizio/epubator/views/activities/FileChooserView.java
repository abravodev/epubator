package it.iiizio.epubator.views.activities;

import java.io.File;

import it.iiizio.epubator.model.entities.FileChooserListItem;

public interface FileChooserView {

    FileChooserListItem getBackItem();

    FileChooserListItem getRootItem();

    FileChooserListItem getUpItem(File currentDirectory);

    FileChooserListItem getRecentFoldersItem();

    String getLastModificationDatetime(File file);

    String getFolderSize();

}
