package com.solarisintel.mp3play;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.IBinder;
import java.util.ArrayList;
import android.content.ContentUris;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.PowerManager;
import android.util.Log;

import java.util.Random;

import android.app.PendingIntent;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private boolean shuffle = false;
    private Random rand;
    private String songTitle = "";
    private static final int NOTIFY_ID = 1;
    private static String CHANNEL_ID = "MyForegroundServiceChannel";
    private static String CHANNEL_NAME = "Channel name";
    private MediaPlayer player;
    private ArrayList<Song> songs;
    public int songPosn;
    public  float  playSpeed  = 1.0f;
    private final IBinder musicBind = new MusicBinder();

    public int repeatCount =  0;
    private int repeatCountDef = 1;

    public void setShuffle() {
        shuffle = !shuffle;
    }

    public void setRepeatCount(int arg) {
        repeatCountDef = arg;
    }

    public int  getRepeatCount() {
        return repeatCountDef;
    }

    public void onCreate() {
        super.onCreate();
        rand = new Random();
        songPosn = 0;
        player = new MediaPlayer();
        initMusicPlayer();
    }

    public void initMusicPlayer() {
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);

    }

    public void setList(ArrayList<Song> theSongs) {
        songs = theSongs;
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return false;
    }

    public void playSong() {
        player.reset();
        Song playSong = songs.get(songPosn);
        songTitle = playSong.getTitle();
        long currSong = playSong.getID();
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);
        Log.d("DEBUG", "play file =" + trackUri.toString());
        try {
            player.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();

        PlayActivity.RedrawListColor(songPosn) ;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mp.getCurrentPosition() > 0) {
            repeatCount += 1;
            if (repeatCount < repeatCountDef) {
                playSong();
            } else {
                mp.reset();
                playNext();
                repeatCount = 0;
            }
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e("MUSIC SERVICE", "Player in Serice error");
        Log.e("MUSIC SERVICE", "what=" + what + " extera=" + extra );
        mp.reset();
        return false;
    }

    public void setSong(int songIndex)
    {
        songPosn = songIndex;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

        // Android 8.0 以降は Notificationの扱いが違う
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW; // IMPORTANCE_LOW:通知音をなしにする
            channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            channel.setDescription("mp3player channel for foreground service notification");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        mp.setPlaybackParams(new PlaybackParams().setSpeed(playSpeed));
        mp.start();

        PlayActivity.playButtonChangePause(true);

        Intent notIntent = new Intent(this, PlayActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.play)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle);
        Notification not = builder.build();

        // android 14 で仕様が変わり manifest.xml に追加が必要
        startForeground(NOTIFY_ID, not);
    }

    public int getPosn() {
        return player.getCurrentPosition();
    }

    public int getDur() {
        return player.getDuration();
    }

    public boolean isPng() {
        return player.isPlaying();
    }

    public void pausePlayer() {
        player.pause();
    }

    public void seek(int posn) {
        player.seekTo(posn);
    }

    public void go() {
        player.setPlaybackParams(new PlaybackParams().setSpeed(playSpeed));
        player.start();
        Log.d("DEBUG", "player start");
    }

    public void playPrev() {
        songPosn--;
        if (songPosn < 0) songPosn = songs.size() - 1;
        PlayActivity.playingPosn = songPosn;
        repeatCount = 0;
        playSong();
    }

    public void playNext() {
        if (shuffle) {
            int newSong = songPosn;
            while (newSong == songPosn) {
                newSong = rand.nextInt(songs.size());
            }
            songPosn = newSong;
        } else {
            songPosn++;
            if (songPosn >= songs.size()) songPosn = 0;
        }

        PlayActivity.playingPosn = songPosn;
        repeatCount = 0;
        playSong();
    }

    public void rewind(int backSec) {
        int nowposition = player.getCurrentPosition();
        Log.d("DEBUG", "music position=" + nowposition);
        nowposition = nowposition - backSec * 1000;
        if (nowposition < 0) {
            nowposition = 0;
        }
        player.seekTo(nowposition);
    }

    public void rewindFrist() {
        player.seekTo(0);
    }

    public void setSpeed() {
        player.setPlaybackParams(new PlaybackParams().setSpeed(playSpeed));
    }
}

