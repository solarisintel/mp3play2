package com.solarisintel.mp3play;

class Folders {
    private String title;
    private String path;
    private int    filesCount;

    Folders(String folderTitle, String folderPath) {
        title = folderTitle;
        path = folderPath;
        filesCount = 1;
    }

    String getTitle() {
        return title;
    }

    String getPath() {
        return path;
    }

    void addFile() {
        filesCount += 1;
    }

    int  getfilesCount() {
        return filesCount;
    }
}