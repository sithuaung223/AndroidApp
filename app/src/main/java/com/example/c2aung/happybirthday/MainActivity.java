package com.example.c2aung.happybirthday;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {
    private ArrayList<Song> songList;
    private ListView songView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

    }
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
}
