package it.iiizio.epubator.presenters;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.iiizio.epubator.model.entities.FileChooserListItem;
import it.iiizio.epubator.views.activities.FileChooserView;

public class FileChooserPresenterImpl implements FileChooserPresenter {

    private final FileChooserView view;

    public FileChooserPresenterImpl(FileChooserView view) {
        this.view = view;
    }

    @Override
    public List<FileChooserListItem> getRecentFoldersItems(String history) {
        List<FileChooserListItem> fileChooserList = new ArrayList<>();
        fileChooserList.add(view.getBackItem());

        String[] folders = history.split("\\|");
        if (folders.length > 0) {
            for(String folder : folders) {
                File file = new File(folder);
                boolean readableFolder = !file.isHidden() && file.canRead() && file.isDirectory();
                if(readableFolder) {
                    FileChooserListItem item = new FileChooserListItem();
                    item.setName(folder);
                    item.setSize(view.getFolderSize());
                    item.setDate(view.getLastModificationDatetime(file));
                    item.setEnabled(true);
                    fileChooserList.add(item);
                }
            }
        }

        return fileChooserList;
    }

    @Override
    public List<FileChooserListItem> getFileItems(String path, String extension, boolean showAllFiles) {
        List<FileChooserListItem> fileChooserList = new ArrayList<>();
        fileChooserList.add(view.getRecentFoldersItem());

        File directory = new File(path + "/");
        if (path.length() > 1) {
            fileChooserList.add(view.getRootItem());
            fileChooserList.add(view.getUpItem(directory));
        }

        File[] files = directory.listFiles();
        if (files.length > 0) {
            Arrays.sort(files);
            for(File file : files) {
                if((!file.isHidden()) && (file.canRead())) {
                    String fileName = file.getName();
                    if (file.isDirectory()) {
                        fileChooserList.add(getFolder(file));
                    } else if (fileName.endsWith(extension)) {
                        fileChooserList.add(getFile(file, true));
                    } else if (showAllFiles) {
                        fileChooserList.add(getFile(file, false));
                    }
                }
            }
        }
        return fileChooserList;
    }

    @Override
    public String newHistory(String path, String history) {
        history = path + "|" + history.replace(path + "|", "");
        String[] items = history.split("\\|");
        if (items.length > 8) {
            StringBuilder sb = new StringBuilder();
            for (int k = 0;  k < 8; k++) {
                sb.append(items[k]);
                sb.append("|");
            }
            history = sb.toString();
        }
        return history;
    }

    private FileChooserListItem getFolder(File folder){
        FileChooserListItem item = new FileChooserListItem();
        item.setName(folder.getName() + "/");
        item.setSize(view.getFolderSize());
        item.setDate(view.getLastModificationDatetime(folder));
        item.setEnabled(true);
        return item;
    }

    private FileChooserListItem getFile(File file, boolean showFile){
        FileChooserListItem item = new FileChooserListItem();
        item.setName(file.getName());
        item.setSize(String.format("%d Byte", file.length()));
        item.setDate(view.getLastModificationDatetime(file));
        item.setEnabled(showFile);
        return item;
    }
}
