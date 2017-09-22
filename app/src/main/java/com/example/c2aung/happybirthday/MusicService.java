package com.example.c2aung.happybirthday;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import java.util.ArrayList;
import android.content.ContentUris;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.util.Log;
import java.util.Random;
import android.app.Notification;
import android.app.PendingIntent;



/**
 * Created by c2aung on 9/10/2017.
 */

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private MediaPlayer player; //meida player
    private ArrayList<Song> songs; //song list
    private int songPosn; //current position
    private final IBinder musicBind = new MusicBinder();
    private String songTitle="";
    private static final int NOTIFY_ID = 1;

    public void onCreate(){
        super.onCreate(); //create the service
        songPosn = 0; // initialize postion
        player = new MediaPlayer(); //create player
        initMusicPlayer();
    }

    public void initMusicPlayer(){
        //set player properties
        player.setWakeMode( getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType( AudioManager.STREAM_MUSIC );
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setList (ArrayList<Song> theSongs){
        songs = theSongs;
    }

    public class MusicBinder extends Binder {
        MusicService getService(){
            return MusicService.this;
        }
    }

    public void playSong() {
        player.reset();
        Song playSong = songs.get(songPosn);
        songTitle = playSong.getTitle();
        long currSong = playSong.getID();
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);
        try{
            player.setDataSource( getApplicationContext(), trackUri);
        }
        catch (Exception e){
            Log.e( "MUSIC SERVOCE", "Error setting data source", e);
        }
        player.prepareAsync();
    }

    public void setSong (int songIndex) {
        songPosn = songIndex;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onPrepared(MediaPlayer mp) {
        //start playback
        mp.start();
        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.play)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle);
        Notification not = builder.build();
        startForeground(NOTIFY_ID,not);
    }
    @Override
    public void onDestroy(){
        stopForeground(true);
    }

    public int getPosn(){
        return player.getCurrentPosition();
    }
    public int getDur(){
        return player.getDuration();
    }
    public boolean isPng(){
        return player.isPlaying();
    }
    public void pausePlayer(){
        player.pause();
    }
    public void seek(int posn){
        player.seekTo(posn);
    }
    public void go(){
        player.start();
    }

    public void playPrev(){
        songPosn--;
        if(songPosn < 0){
            songPosn = songs.size() - 1;
        }
        playSong();
    }
    public void playNext(){
        songPosn++;
        if(songPosn >= songs.size()){
            songPosn = 0;
        }
        playSong();
    }

}
