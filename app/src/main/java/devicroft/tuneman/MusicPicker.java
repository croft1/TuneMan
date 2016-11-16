package devicroft.tuneman;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

public class MusicPicker extends AppCompatActivity {

    ListView songListView;
    SongAdapter songAdapter;
    ArrayList<Song> songList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_picker);


        songList = new ArrayList<>();

        //find audio that fits the parameters and get its info put into a list
        getAllSongs();


        songListView = (ListView) findViewById(R.id.music_list);
        //create the adapter and set it with the list of songs we found
        songAdapter = new SongAdapter(this, songList);
        songListView.setAdapter(songAdapter);
        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                dispatchSongSelectIntent(i);
                finish();
                overridePendingTransition(0,0);
            }
        });



    }

    private void dispatchSongSelectIntent(int selected) {
        //put through index to reference in main
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra(MainActivity.EXTRA_SONG_INDEX_PATH, songList.get(selected).getPath());
        i.putExtra("object", songList.get(selected));
        setResult(RESULT_OK, i);
    }

    private void getAllSongs(){
        //https://stackoverflow.com/questions/34847614/getting-all-music-files
        //https://code.tutsplus.com/tutorials/create-a-music-player-on-android-project-setup--mobile-22764

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        //get cursor to navigate through the audio database
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if(cursor != null && cursor.moveToFirst()){
            do{
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME) );
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String length = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                ///create song for list
                songList.add(new Song(title, path, length));

                Log.d("Song found: ", title + " " + path );
            }while(cursor.moveToNext());
        }
        cursor.close();
    }
}
