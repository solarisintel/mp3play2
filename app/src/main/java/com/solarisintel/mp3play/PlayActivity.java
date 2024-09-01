package com.solarisintel.mp3play;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.MediaController;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class PlayActivity extends AppCompatActivity
{

    private boolean playPaused;
    public static MediaController controller;
    public MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound = false;

    public ArrayList<Song> playSongList;
    static private ListView songListView;

    // 再生中のポジション, SERVICE側で呼ばれている
    public static int  playingPosn = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        // show back botton
        assert getSupportActionBar() != null;   // null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button
        getSupportActionBar().setTitle(MainActivity.SelectedFolders.getTitle());
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.lavender, getTheme())));

        // collect play song list
        playSongList = new ArrayList<>();
        for (int i = 0; i < MainActivity.songList.size(); i++ ) {
            if (MainActivity.SelectedFolders.getPath().equals(MainActivity.songList.get(i).getParentPath())) {
                playSongList.add(MainActivity.songList.get(i));
            }
        }

        Collections.sort(playSongList, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        songListView = findViewById(R.id.song_list);

        SongAdapter songAdt = new SongAdapter(this, playSongList);
        songListView.setAdapter(songAdt);

        // BottomNavigationViewのイベント設定
        setNavViewSpeed();

        // Bottom Menuのイベント設定
        addListenerOnButton();

        // 再生中のMP3ファイルの背景色を変更する。手でスクロールした際に追従させる
        songListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                // scrollState=1	stated
                // scrollState=2	flick over
                // scrollState=0	scroll stopped
                if(scrollState == 0) {
                    Log.d("DEBUG", "scrolling stopped...");
                    if (playingPosn >= 0) {
                        RedrawListColor(playingPosn);
                    }
                }
            }
            @Override
            public void onScroll(AbsListView absListView,
                                 int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //if (playingPosn >= 0) {
                //    RedrawListColor(playingPosn);
                //}
            }

        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
            Log.d("DEBUG","music service start");
        }
    }

    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicSrv = null;
        super.onDestroy();
    }

    // BottomNavigationViewのイベント設定
    private void setNavViewSpeed(){
        BottomNavigationView navView = findViewById(R.id.bottom_nav_bar);

        navView.setSelectedItemId(R.id.action_normal);

        navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.action_hurry) {
                    if (musicSrv != null && musicBound) {
                        musicSrv.playSpeed = 1.5f;
                        if (musicSrv.isPng()) {
                            musicSrv.setSpeed();
                            musicStart();
                        }
                        return true;
                    }
                }
                if (itemId == R.id.action_turtle) {
                    if (musicSrv != null && musicBound) {
                        musicSrv.playSpeed = 0.75f;
                        if (musicSrv.isPng()) {
                            musicSrv.setSpeed();
                            musicStart();
                        }
                        return true;
                    }
                }
                if (itemId == R.id.action_snail) {
                    if (musicSrv != null && musicBound) {
                        musicSrv.playSpeed = 0.5f;
                        if (musicSrv.isPng()) {
                            musicSrv.setSpeed();
                            musicStart();
                        }
                        return true;
                    }
                }
                if (itemId == R.id.action_normal) {
                    if (musicSrv != null && musicBound) {
                        musicSrv.playSpeed = 1.0f;
                        if (musicSrv.isPng()) {
                            musicSrv.setSpeed();
                            musicStart();
                        }
                        return true;
                    }
                }
                return true;
            }
        });
    }

     static public void RedrawListColor(int posn) {

        Log.d("DEBUG", "RedrawListColor called positon=" + posn);

        int first = songListView.getFirstVisiblePosition();
        int last = songListView.getLastVisiblePosition();

        View targetView;
        View targetImageView;
        for (int i = first; i <= last; i++) {
            targetView = songListView.getChildAt(i - first);
            targetImageView = targetView.findViewById(R.id.image);
            if (posn == i) {
                targetView.setBackgroundColor(Color.parseColor("#444444"));
                targetImageView.setBackgroundResource(R.drawable.white_playing48);
            } else {
                // playing line is reset, I think faster than replacing everything all
                ColorDrawable colorDrawable = (ColorDrawable) targetView.getBackground();
                if (colorDrawable.getColor() == Color.parseColor("#444444")) {
                    targetView.setBackgroundColor(Color.parseColor("#333333"));
                    targetImageView.setBackgroundResource(R.drawable.white_play48);
                }
            }
        }
    }

    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicSrv = binder.getService();
            musicSrv.setList(playSongList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    // action bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        // repeat change icon
        if (itemId == R.id.action_repeat) {
            if (musicSrv.getRepeatCount() == 1) {
                item.setIcon(R.drawable.repeat3);
                musicSrv.setRepeatCount(3);
            } else {
                item.setIcon(R.drawable.repeat1);
                musicSrv.setRepeatCount(1);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    // action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_play, menu);
        return true;
    }

    // Backbutton on action bar, play stop
    @Override
    public boolean onSupportNavigateUp() {
        stopService(playIntent);
        musicSrv = null;
        finish();
        return true;
    }

    private void musicStart() {
        playPaused = false;
        musicSrv.go();
        playButtonChangePause(true);

    }

    private void musicPause() {
        playPaused = true;
        musicSrv.pausePlayer();
        playButtonChangePause(false);
    }

    // listview clicked, function name is defined in layout.xml
    public void songPicked(View view) {
        int position = Integer.parseInt(view.getTag().toString());
        musicSrv.setSong(position);
        playingPosn = position;
        Log.d("DEBUG", "play position=" + position + " title =" + playSongList.get(position).getTitle());
        musicSrv.repeatCount = 0;
        musicSrv.playSong();
        playPaused = false;
    }

    //
    // bottom menu control
    //
    private ImageButton bottomMenuBtn1;
    private ImageButton bottomMenuBtn2;
    private ImageButton bottomMenuBtn3;
    public static ImageButton bottomMenuBtn4;
    private ImageButton bottomMenuBtn5;

    // 最下行ボタンのリスナー登録
    public void addListenerOnButton() {

        // rewind first
        bottomMenuBtn1 = (ImageButton) findViewById(R.id.BottomMenuBtn1);
        bottomMenuBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                musicSrv.rewindFrist();
            }
        });

        // rewind 10 sec
        bottomMenuBtn2 = (ImageButton) findViewById(R.id.BottomMenuBtn2);
        bottomMenuBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                musicSrv.rewind(10);
            }
        });

        // rewind 5 sec
        bottomMenuBtn3 = (ImageButton) findViewById(R.id.BottomMenuBtn3);
        bottomMenuBtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                musicSrv.rewind(5);
            }
        });

        // play/pause
        bottomMenuBtn4 = (ImageButton) findViewById(R.id.BottomMenuBtn4);
        bottomMenuBtn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (playPaused == true) {
                    musicStart();
                } else {
                    musicPause();
                }
            }
        });

        // play_next
        bottomMenuBtn5 = (ImageButton) findViewById(R.id.BottomMenuBtn5);
        bottomMenuBtn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                musicSrv.playNext();
            }
        });

    }

    // play bottom change to play or pause
    public static  void playButtonChangePause(boolean arg){
        if (arg == true) {
            bottomMenuBtn4.setBackgroundResource(R.drawable.btn_music_pause);
        } else {
            bottomMenuBtn4.setBackgroundResource(R.drawable.btn_music_play);
        }
    }

}
