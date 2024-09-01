package com.solarisintel.mp3play;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

class FolderAdapter extends BaseAdapter {

    private ArrayList<Folders> folders;
    private LayoutInflater folderInf;

    FolderAdapter(Context c, ArrayList<Folders> theFolders) {
        folders = theFolders;
        folderInf = LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return folders.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout folderLay = (LinearLayout) folderInf.inflate
                (R.layout.folder, parent, false);
        TextView folderView = (TextView) folderLay.findViewById(R.id.folder_title);
        TextView pathView = (TextView) folderLay.findViewById(R.id.folder_path);
        TextView filesView = (TextView) folderLay.findViewById(R.id.files_count);

        Folders currFolder = folders.get(position);

        folderView.setTextSize(12);
        folderView.setMaxLines(1);
        folderView.setText(currFolder.getTitle());

        pathView.setMaxLines(1);
        pathView.setTextSize(10);
        pathView.setText(currFolder.getPath());

        filesView.setText("(" + currFolder.getfilesCount() + ")");
        filesView.setTextSize(12);

        folderLay.setTag(position);

        return folderLay;
    }

}
