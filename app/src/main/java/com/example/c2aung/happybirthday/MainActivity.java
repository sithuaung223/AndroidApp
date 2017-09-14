package com.example.c2aung.happybirthday;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.view.Menu;
import android.widget.ListView;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.MenuItem;
import android.view.View;
import com.example.c2aung.happybirthday.MusicService.MusicBinder; // import binder from another class aka MusicService
import android.widget.MediaController.MediaPlayerControl;

public class MainActivity extends AppCompatActivity implements MediaPlayerControl {
    private ArrayList<Song> songList;
    private ListView songView;
    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound = false;
    private MusicController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);

                // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                // app-defined int constant

                return;
            }}
        songView = (ListView)findViewById(R.id.song_list);
        songList = new ArrayList<Song>();

        //get the list of song
        getSongList();

        // sorting the song alphabetically
        Collections.sort(songList, new Comparator<Song>(){
            public int compare(Song a, Song b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        SongAdapter songAdt = new SongAdapter (this, songList);
        songView.setAdapter(songAdt);
        setController();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //to fix the menu icon appear on actionbar
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected  void onStart() {
        super.onStart();
        if (playIntent == null){
            playIntent = new Intent(this, MusicService.class);
            bindService( playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService( playIntent);
        }
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item){
        //menu item selected
        switch (item.getItemId()){
            case R.id.action_shuffle:
                //shuffle
                break;
            case R.id.action_end:
                stopService( playIntent);
                musicSrv = null;
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected  void onDestroy() {
        stopService( playIntent);
        musicSrv=null;
        super.onDestroy();
    }

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder) service;
            musicSrv = binder.getService(); //get service
            musicSrv.setList(songList); // pass List
            musicBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName name){
            musicBound = false;
        }
    };

    public void getSongList(){
        //retrive song info
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri,null,null,null,null);

        if ( musicCursor!=null && musicCursor.moveToFirst()){
            int titleColumn = musicCursor.getColumnIndex( MediaStore.Audio.Media.TITLE);
            int idColumn    = musicCursor.getColumnIndex( MediaStore.Audio.Media._ID);
            int artistColumn= musicCursor.getColumnIndex( MediaStore.Audio.Media.ARTIST);
            //add song to list
            do{
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle= musicCursor.getString(titleColumn);
                String thisArtist= musicCursor.getString(artistColumn);
                songList.add( new Song( thisId, thisTitle, thisArtist));

            } while (musicCursor.moveToNext());
        }

    }

    public void songPicked (View view) {
        musicSrv.setSong( Integer.parseInt(view.getTag().toString()));
        musicSrv.playSong();
    }

    //setting up controller
    private void setController() {
        controller = new MusicController(this);
        controller.setPrevNextListeners(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);
    }
    //play next
    private void playNext(){
        musicSrv.playNext();
        controller.show(0);
    }
    // play previous
    private void playPrev(){
        musicSrv.playPrev();
        controller.show(0);
    }
    @Override
    public void start() {
        musicSrv.go();
    }

    @Override
    public void pause() {
        musicSrv.pausePlayer();
    }

    @Override
    public int getDuration() {
        if(musicSrv != null && musicBound && musicSrv.isPng()){
            return musicSrv.getDur();
        }else{
            return 0;
        }
    }

    @Override
    public int getCurrentPosition() {
        if(musicSrv != null && musicBound && musicSrv.isPng() ){
            return musicSrv.getPosn();
        }else{
            return 0;
        }
    }

    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if(musicSrv != null && musicBound){
            return musicSrv.isPng();
        }else{
            return false;
        }
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
}
