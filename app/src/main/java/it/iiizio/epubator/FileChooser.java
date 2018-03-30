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

package it.iiizio.epubator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FileChooser extends ListActivity {
	private	String path = "/";
	private	String filter = "";
	private ListView lv;
	private boolean showAllFiles;
	private boolean hideDetail;
	private String history;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filechoosermain);
		
		// Get extras
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

		// Update path history
		SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
		history = sharedPref.getString("history", "");
		SharedPreferences.Editor editor = sharedPref.edit();
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
		editor.putString("history", history);
		editor.commit();

		// Set up ListView
		lv = getListView();
		lv.setTextFilterEnabled(true);
		lv.setOnItemClickListener(new OnItemClickListener() {
			// New OnItemClickListener
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String chosen = ((TextView) view.findViewById(R.id.name)).getText().toString();
				
				if(chosen.endsWith("/")) {
					// Change path
					if (chosen == "/") {
						path = chosen;
					} else if (chosen == "../") {
						path = path.substring(0, path.lastIndexOf('/', path.length() - 2) + 1);
					} else if (chosen.startsWith("/")) {
						path = chosen;
					} else {
						path += chosen;
					}
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

		// Show FileChooser
		setFileList(path, filter);
	}
	
	// Fill FileChooser
	private void setFileList(String dirPath, String ext) {
		ArrayList<FileChooserList> fileChooserList = new ArrayList<FileChooserList>();
		FileChooserList item;
		File f = new File(dirPath + "/");

		// Get preferences
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
		showAllFiles = prefs.getBoolean("show_all_files", false);
		hideDetail = prefs.getBoolean("hide_detail", false);

		// Add Recent folders
		item = new FileChooserList();
		item.setName(getResources().getString(R.string.recent_title));
		item.setSize(getResources().getString(R.string.recent_summary));
		item.setDate("");
		item.setEnabled(true);
		fileChooserList.add(item);

		// Add Root & Up
		if (dirPath.length() > 1) {
			item = new FileChooserList();
			item.setName("/");
			item.setSize(getResources().getString(R.string.root));
			item.setDate(getDateTime(new File("/")));
			item.setEnabled(true);
			fileChooserList.add(item);
			item = new FileChooserList();
			item.setName("../");
			item.setSize(getResources().getString(R.string.up));
			item.setDate(getDateTime(f.getParentFile()));
			item.setEnabled(true);
			fileChooserList.add(item);
		}

		// Add filenames
		File[] files = f.listFiles(); 
		if (files.length > 0) {
			Arrays.sort(files);
			for(File file : files) {
				if((!file.isHidden()) && (file.canRead())) {
					String fileName = file.getName();
					if (file.isDirectory()) {
						// Folder
						item = new FileChooserList();
						item.setName(fileName + "/");
						item.setSize(getResources().getString(R.string.folder));
						item.setDate(getDateTime(file));
						item.setEnabled(true);
						fileChooserList.add(item);
					} else if (fileName.endsWith(ext)) {
						// Target file
						item = new FileChooserList();
						item.setName(fileName);
						item.setSize(String.format("%d Byte", file.length()));
						item.setDate(getDateTime(file));
						item.setEnabled(true);
						fileChooserList.add(item);
					} else if (showAllFiles) {
						// Other file
						item = new FileChooserList();
						item.setName(fileName);
						item.setSize(String.format("%d Byte", file.length()));
						item.setDate(getDateTime(file));
						item.setEnabled(false);
						fileChooserList.add(item);
					}
				}
			}
		}

		// Update screen
		lv.clearTextFilter();
		((TextView) findViewById(R.id.path)).setText(String.format(getResources().getString(R.string.path), path));
        lv.setAdapter(new FileChooserAdapter(this, fileChooserList));
	}
	
	// Fill Recent folders
	private void setRecentFolders() {
		ArrayList<FileChooserList> fileChooserList = new ArrayList<FileChooserList>();
		FileChooserList item;
		
		// Get preferences
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
		showAllFiles = prefs.getBoolean("show_all_files", false);

		// Add Back
		item = new FileChooserList();
		item.setName(getResources().getString(R.string.back_title));
		item.setSize(getResources().getString(R.string.back_summary));
		item.setDate("");
		item.setEnabled(true);
		fileChooserList.add(item);

		// Add filenames
		String[] folders = history.split("\\|");
		if (folders.length > 0) {
			for(String folder : folders) {
				File file = new File (folder);
				if((!file.isHidden()) && (file.canRead())) {
					if (file.isDirectory()) {
						// Folder
						item = new FileChooserList();
						item.setName(folder);
						item.setSize(getResources().getString(R.string.folder));
						item.setDate(getDateTime(file));
						item.setEnabled(true);
						fileChooserList.add(item);
					}
				}
			}
		}

		// Update screen
		lv.clearTextFilter();
		((TextView) findViewById(R.id.path)).setText(String.format(getResources().getString(R.string.path), path));
        lv.setAdapter(new FileChooserAdapter(this, fileChooserList));
	}
	
	// Get last modification date & time
	String getDateTime(File file) {
		String date = DateUtils.formatDateTime(this, file.lastModified(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE);
		String time = DateUtils.formatDateTime(this, file.lastModified(), DateUtils.FORMAT_SHOW_TIME);
		return String.format("%s %s", date, time);
	}

	// Custom list class
	public class FileChooserList {
		private String name = "";
		private String size = "";
		private String date = "";
		private boolean enabled;

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setSize(String size) {
			this.size = size;
		}

		public String getSize() {
			return size;
		}

		public void setDate(String date) {
			this.date = date;
		}

		public String getDate() {
			return date;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public boolean getEnabled() {
			return enabled;
		}
	}

	// Custom adapter
	public class FileChooserAdapter extends BaseAdapter {
		private  ArrayList<FileChooserList> fileChooserList;

		private LayoutInflater mInflater;

		public FileChooserAdapter(Context context, ArrayList<FileChooserList> results) {
			fileChooserList = results;
			mInflater = LayoutInflater.from(context);
		}

		public int getCount() {
			return fileChooserList.size();
		}

		public Object getItem(int position) {
			return fileChooserList.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.filechooserrow, null);
				holder = new ViewHolder();
				holder.txtName = (TextView) convertView.findViewById(R.id.name);
				holder.txtSize = (TextView) convertView.findViewById(R.id.size);
				holder.txtDate = (TextView) convertView.findViewById(R.id.date);
				holder.row = (RelativeLayout) convertView.findViewById(R.id.row);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.txtName.setText(fileChooserList.get(position).getName());
			holder.txtSize.setText(fileChooserList.get(position).getSize());
			holder.txtDate.setText(fileChooserList.get(position).getDate());
			
			boolean enabled = fileChooserList.get(position).getEnabled();
			holder.row.setClickable(!enabled);
			holder.row.setFocusable(!enabled);
			holder.row.setEnabled(enabled);
			holder.txtName.setEnabled(enabled);
			
			if (hideDetail) {
				holder.txtSize.setVisibility(View.GONE);
				holder.txtDate.setVisibility(View.GONE);
			}

			return convertView;
		}
		
		class ViewHolder {
			TextView txtName;
			TextView txtSize;
			TextView txtDate;
			RelativeLayout row;
		}
	}
}