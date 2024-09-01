package com.solarisintel.mp3play;

import java.io.File;

class Song {
    private long id;
    private String title;
    private String artist;
    private String path;
    private String parentPath;
    private String album;
    private String basename;

    Song(long songID, String songTitle, String songArtist, String filePath, String argAlbum) {
        id = songID;
        title = songTitle;
        artist = songArtist;
        path = filePath;
        album = argAlbum;

        File fPath = new File(filePath);
        basename = fPath.getName();
        parentPath = fPath.getParent();

    }

    long getID() {
        return id;
    }

    String getTitle() {
        return title;
    }

    String getArtist() {
        return artist;
    }

    String getPath() { return path;}

    String getParentPath() { return parentPath;}

    String getBaseName() {return basename;}
}