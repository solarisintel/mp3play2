
original source
https://gist.github.com/sunmeat/319e4395b547869db334cd9f5619d2ba

かなりバージョンが低いソース、5か6か
起動時の権限付与がされていない

fix1)
import package com.alex.player.MusicService.MusicBinder;
->
import com.alex.player.MusicService.MusicBinder;


fix2)
Duplicate class android.support.v4.app.INotificationSideChannel ...
-> 
add gradle.properties
android.useAndroidX=true
android.enableJetifier=true

fix3)
@Override
public boolean onOptionsItemSelected(MenuItem item) {
switch (item.getItemId()) {
    case R.id.action_shuffle:
    musicSrv.setShuffle();
    break;
->
public boolean onOptionsItemSelected(MenuItem item) {
int itemId = item.getItemId();
    if (itemId ==  R.id.action_shuffle) {
       musicSrv.setShuffle();
    } else if (itemId == R.id.action_end) {


Fix4)
ビルド時のメッセージ文字化け
->
メニュー＞Help＞Edit Custom VM Options
「-Dfile.encoding=UTF-8」を追加。
Android Studio 再起動


Fix5)
android.app.RemoteServiceException:
Bad notification for startForeground: java.lang.RuntimeException: invalid channel for service notification:
Notification(channel=null pri=0 contentView=null vibrate=null sound=null tick defaults=0x0 flags=0x42 color=0x00000000 vis=PRIVATE)

see) https://qiita.com/park3taro/items/455a21bc1a9119271d5d

    private static final int NOTIFY_ID = 1;
    private static String CHANNEL_ID = "MyForegroundServiceChannel";
    private static String CHANNEL_NAME = "Channel name";

       if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            channel.setDescription("mp3player channel for foreground service notification");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
       }
       Notification.Builder builder;
       if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
       } else {
            builder = new Notification.Builder(this);
       }
 