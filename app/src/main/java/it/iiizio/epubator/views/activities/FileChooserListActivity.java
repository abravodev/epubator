/*
Copyright (C)2011 Ezio Querini <iiizio AT users.sf.net>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.iiizio.epubator.views.activities;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import it.iiizio.epubator.R;
import it.iiizio.epubator.model.entities.FileChooserListItem;
import it.iiizio.epubator.presenters.FileChooserPresenter;
import it.iiizio.epubator.presenters.FileChooserPresenterImpl;
import it.iiizio.epubator.views.adapters.FileChooserAdapter;

public class FileChooserListActivity extends ListActivity implements FileChooserView {

	private	String path = "/";
	private	String filter = "";
	private ListView lv_fileItems;
	private boolean showAllFiles;
	private boolean hideDetail;
	private String history;
	private FileChooserPresenter presenter;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filechoosermain);
		presenter = new FileChooserPresenterImpl(this);

		setupPathAndFilter();

		updatePathHistory();
		setUpListView();
		setFileList(path, filter);
	}

	private void setupPathAndFilter() {
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if (extras.containsKey("path")) {
				File pathFile = new File(extras.getString("path"));
				if (pathFile.exists() && pathFile.isDirectory() && pathFile.canRead()) {
					path = pathFile.getPath();
					if (!path.endsWith("/")) {
						path += "/";
					}
				}
			}
			if (extras.containsKey("filter")) {
				filter = extras.getString("filter");
			}
		}
	}

	private void setUpListView() {
		lv_fileItems = getListView();
		lv_fileItems.setTextFilterEnabled(true);
		lv_fileItems.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String chosen = ((TextView) view.findViewById(R.id.name)).getText().toString();
				if(chosen.endsWith("/")) {
					updatePath(chosen);
					setFileList(path, filter);

				} else if (chosen == getResources().getString(R.string.recent_title)) {
					setRecentFolders();
				} else if (chosen == getResources().getString(R.string.back_title)) {
					setFileList(path, filter);
				} else {
					// File chosen
					setResult(RESULT_OK, (new Intent()).setAction(path + chosen));
					finish();
				}
			}
		});
	}

	private void updatePath(String chosen) {
		if (chosen == "/") {
            path = chosen;
        } else if (chosen == "../") {
            path = path.substring(0, path.lastIndexOf('/', path.length() - 2) + 1);
        } else if (chosen.startsWith("/")) {
            path = chosen;
        } else {
            path += chosen;
        }
	}

	private void updatePathHistory() {
		SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
		history = sharedPref.getString("history", "");
		SharedPreferences.Editor editor = sharedPref.edit();
		history = presenter.newHistory(path, history);
		editor.putString("history", history);
		editor.commit();
	}

	private void setFileList(String dirPath, String ext) {
		List<FileChooserListItem> fileChooserList = presenter.getFileItems(dirPath, ext, showAllFiles);
		updateFileChooserList(fileChooserList);
	}

	private void setRecentFolders() {
		List<FileChooserListItem> fileChooserList = presenter.getRecentFoldersItems(history);
		updateFileChooserList(fileChooserList);
	}

	private void getPreferences() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		showAllFiles = prefs.getBoolean("show_all_files", false);
		hideDetail = prefs.getBoolean("hide_detail", false);
	}

	private void updateFileChooserList(List<FileChooserListItem> fileChooserList){
		getPreferences();

		lv_fileItems.clearTextFilter();
		((TextView) findViewById(R.id.path)).setText(String.format(getResources().getString(R.string.path), path));
		lv_fileItems.setAdapter(new FileChooserAdapter(this, fileChooserList, hideDetail));
	}

	@Override
	public String getFolderSize(){
		return getResources().getString(R.string.folder);
	}

	@Override
	public String getLastModificationDatetime(File file) {
		String date = DateUtils.formatDateTime(this, file.lastModified(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE);
		String time = DateUtils.formatDateTime(this, file.lastModified(), DateUtils.FORMAT_SHOW_TIME);
		return String.format("%s %s", date, time);
	}

	@Override
	public FileChooserListItem getBackItem() {
		FileChooserListItem item = new FileChooserListItem();
		item.setName(getResources().getString(R.string.back_title));
		item.setSize(getResources().getString(R.string.back_summary));
		item.setDate("");
		item.setEnabled(true);
		return item;
	}

	@Override
	public FileChooserListItem getRootItem() {
		FileChooserListItem item = new FileChooserListItem();
		item.setName("/");
		item.setSize(getResources().getString(R.string.root));
		item.setDate(getLastModificationDatetime(new File("/")));
		item.setEnabled(true);
		return item;
	}

	@Override
	public FileChooserListItem getUpItem(File currentDirectory) {
		FileChooserListItem item = new FileChooserListItem();
		item.setName("../");
		item.setSize(getResources().getString(R.string.up));
		item.setDate(getLastModificationDatetime(currentDirectory.getParentFile()));
		item.setEnabled(true);
		return item;
	}

	@Override
	public FileChooserListItem getRecentFoldersItem() {
		FileChooserListItem item = new FileChooserListItem();
		item.setName(getResources().getString(R.string.recent_title));
		item.setSize(getResources().getString(R.string.recent_summary));
		item.setDate("");
		item.setEnabled(true);
		return item;
	}
}