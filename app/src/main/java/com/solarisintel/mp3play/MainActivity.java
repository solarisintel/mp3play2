package com.solarisintel.mp3play;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.solarisintel.mp3play.MusicService.MusicBinder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.PlaybackParams;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.View;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.view.MenuItem;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.os.HandlerCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
public class MainActivity extends AppCompatActivity {

    private ArrayList<Folders> foldersList;
    static private ListView folderListView;

    public static ArrayList<Song> songList;

    public static Folders SelectedFolders;

    ArrayList<String> requestPermissions = new ArrayList<>();
    private static final int REQUEST_MULTI_PERMISSIONS = 101;

    private void checkPermissions() {
        ArrayList<String> requestPermissions = new ArrayList<>();
        // MediaStore
        if (Build.VERSION.SDK_INT > 32) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions.add(Manifest.permission.READ_MEDIA_AUDIO);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
        if (!requestPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, requestPermissions.toArray(new String[0]), REQUEST_MULTI_PERMISSIONS);
        }
    }

    private boolean PermissonGrantedApp = true;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_MULTI_PERMISSIONS) {
            if (grantResults.length > 0) {
                for (int i = 0; i < permissions.length; i++) {
                    switch (permissions[i]) {
                        case Manifest.permission.READ_EXTERNAL_STORAGE:
                        case Manifest.permission.READ_MEDIA_AUDIO:
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                PermissonGrantedApp = false;
                            }
                            break;
                        default:
                    }
                }
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // permission request
        checkPermissions();

        songList = new ArrayList<>();
        foldersList = new ArrayList<>();
        ListView folderView = findViewById(R.id.folder_list);

        if (PermissonGrantedApp == false) {
            return;
        }

        getSongList();

        Collections.sort(foldersList, new Comparator<Folders>() {
            public int compare(Folders a, Folders b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        Collections.sort(songList, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        FolderAdapter folderAdt = new FolderAdapter(this, foldersList);
        folderView.setAdapter(folderAdt);

        assert getSupportActionBar() != null;   // null check
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.lavender, getTheme())));

    }


    public void getSongList() {
        int songListCount = 0;
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        Log.d("DEBUG", "EXTERNAL_CONTENT_URI = " + musicUri.toString());
        if (musicCursor != null && musicCursor.moveToFirst()) {
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int albumColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);
            int pathColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.DATA);

            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisPath = musicCursor.getString(pathColumn);
                String thisAlbum = musicCursor.getString(albumColumn);

                int len = thisPath.length();
                String right4 = thisPath.substring(len - 4, len);
                // skip wav files
                if (right4.equals(".mp3") || right4.equals(".MP3")) {
                    songList.add(new Song(thisId, thisTitle, thisArtist, thisPath, thisAlbum));
                    songListCount++;
                    // add folder list
                    addFolders(thisPath);
                }
            }
            while (musicCursor.moveToNext());
        }
        Log.d("DEBUG", "mp3 file   count is  = " + songList.size());
        Log.d("DEBUG", "mp3 folder count is  = " + foldersList.size());
    }

    private void addFolders(String mp3filePath) {
        File fPath = new File(mp3filePath);
        String folderPath = fPath.getParent();
        File pPath = new File(folderPath);
        String Title = pPath.getName();
        boolean exists = false;
        for (int i = 0; i < foldersList.size(); i++ ) {
            if (foldersList.get(i).getPath().equals(folderPath)) {
                exists = true;
                foldersList.get(i).addFile();
                break;
            }
        }
        if (exists == false) {
            foldersList.add(new Folders(Title, folderPath));
            Log.d("DEBUG", "title = " + Title + " path = " + folderPath);
        }

    }

    // this function is defined on layout.xml
    // clicked on listview, change to player activity
    public void folderPicked(View view) {
        int position = Integer.parseInt(view.getTag().toString());
        SelectedFolders = foldersList.get(position);

        Intent myIntent = new Intent(this, PlayActivity.class);
        startActivity(myIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId ==  R.id.action_setting) {
            // not yet implemented
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
