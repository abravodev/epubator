package it.iiizio.epubator.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import it.iiizio.epubator.R;
import it.iiizio.epubator.model.entities.FileChooserListItem;

public class FileChooserAdapter extends BaseAdapter {

    private List<FileChooserListItem> fileChooserList;

    private LayoutInflater mInflater;
    private boolean hideDetail;

    public FileChooserAdapter(Context context, List<FileChooserListItem> results, boolean hideDetail) {
        fileChooserList = results;
        mInflater = LayoutInflater.from(context);
        this.hideDetail = hideDetail;
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
