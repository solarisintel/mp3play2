package com.solarisintel.mp3play;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.BaseAdapter;

class SongAdapter extends BaseAdapter {

    private ArrayList<Song> songs;
    private LayoutInflater songInf;

    SongAdapter(Context c, ArrayList<Song> theSongs) {
        songs = theSongs;
        songInf = LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return songs.size();
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
        LinearLayout songLay = (LinearLayout) songInf.inflate
                (R.layout.song, parent, false);
        TextView songView = (TextView) songLay.findViewById(R.id.song_title);
        TextView pathView = (TextView) songLay.findViewById(R.id.song_artist);
        Song currSong = songs.get(position);

        songView.setMaxLines(1);
        songView.setTextSize(12);
        songView.setText(currSong.getTitle());

        pathView.setTextSize(10);
        pathView.setMaxLines(1);
        pathView.setText(currSong.getBaseName());

        songLay.setTag(position);

        return songLay;
    }

}
