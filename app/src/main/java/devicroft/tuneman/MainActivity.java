package devicroft.tuneman;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Dimension;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.TimeUtils;
import android.util.TimeUtils.*;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import rm.com.audiowave.AudioWaveView;


public class MainActivity extends AppCompatActivity {


    private static final int REQUEST_READ_STORAGE_PERMISSION = 1000;
    ImageView ppButton;
    ImageView nextButton;
    ImageView prevButton;
    TextView time;
    TextView title;
    MP3Player player;

    AudioWaveView waveView;

    Handler handler;
    Thread timeThread;

    MusicService.MusicBinder musicService = null;

    public static final int REQUEST_SELECT_MUSIC = 1;
    public static final String EXTRA_SONG_INDEX_PATH = "song";
    public static final String EXTRA_SONG_PROGRESS = "progress";
    public static final String START_FOREGROUND = "begin";



    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d("Service", "S Connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d("Service", "S Disconnected");
        }
    };



    private View.OnClickListener ppListener = new View.OnClickListener() {
        public void onClick(View v) {
            //not loaded, go to choose song or if it is change UI and play song
            if(player.isLoaded()) {
                updatePlayStatus();
            }else{
                initiateLoadSongActivity();
            }
        }
    };



    //https://stackoverflow.com/questions/21447798/how-to-display-current-time-of-song-in-textview
    //https://developer.android.com/guide/topics/media/mediaplayer.html

    private final Runnable updateSongTimeTask = new Runnable() {
        @Override
        public void run() {

            // Displaying Total Duration time running together
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    time.setVisibility(View.VISIBLE);
                    time.setText("" + TimeUnit.MILLISECONDS.toMinutes(player.getProgress()) +
                            ":" + TimeUnit.MILLISECONDS.toSeconds(player.getProgress()));

                    /*
                    getting byte data too annoying
                    Double progressPercent = new Double(player.getProgress() / player.mediaPlayer.getDuration());
                    waveView.setRawData(player.mediaPlayer.get);
                    waveView.setProgress(progressPercent.intValue());
                    */

                    //circularSlider.setAngle(player.mediaPlayer.getCurrentPosition());
                    //circularSlider.setPosition(player.mediaPlayer.getCurrentPosition());
                }
            });

            // Running this thread after 100 milliseconds
            handler.postDelayed(this, 100);
            /*
            Log.d("time task", TimeUnit.MILLISECONDS.toMinutes(player.getProgress()) +
                    ":" + TimeUnit.MILLISECONDS.toSeconds(player.getProgress()));
            */
        }
    };

    @Override
    public ComponentName startService(Intent service) {
        //send off intent for service start
        Intent serviceIntent   = new Intent(MainActivity.this, MusicService.class);
        serviceIntent.setAction(START_FOREGROUND);
        if(player.getState() != MP3Player.MP3PlayerState.STOPPED ||
                player.getState() != MP3Player.MP3PlayerState.ERROR){
             service.putExtra(EXTRA_SONG_INDEX_PATH, player.getFilePath());
        }
        startService(serviceIntent);
        return null;
    }


    private void updatePlayStatus(){
        //syncronise ui and audio elements
        //we flip the status
        if(player.getState() == MP3Player.MP3PlayerState.PAUSED || player.getState() == MP3Player.MP3PlayerState.STOPPED){
            //not playing, want to play file
            Log.d("PlayB", "Playing");

            //set pause button, as button reflects the opposite of current status
            player.play();
            //player.mediaPlayer.setLooping(true);
            ppButton.setImageResource(R.drawable.pause);

            timeThread = new Thread(updateSongTimeTask);
            timeThread.start();


        }else{
            //is we are playing, we pause it
            Log.d("PlayB", "Pausing");
            ppButton.setImageResource(R.drawable.play);
            player.pause();

            //to cancel the timethread if runing
            try {
                timeThread.join();
            }catch(InterruptedException e){
                e.printStackTrace();
            }catch(NullPointerException e){
                //no other thread to join
                e.printStackTrace();
            }



        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setup references to ui eleements
        ppButton = (ImageView) findViewById(R.id.play_pause);
        nextButton = (ImageView) findViewById(R.id.next);
        prevButton = (ImageView) findViewById(R.id.previous);
        time = (TextView) findViewById(R.id.timeText);
        title = (TextView) findViewById(R.id.title);
        ppButton = (ImageView) findViewById(R.id.play_pause);
        //waveView = (AudioWaveView) findViewById(R.id.wave);
        player = new MP3Player();

        //setup waveview
        waveView.setWaveColor(R.color.colorAccent);


        ppButton.setOnClickListener(ppListener);

        //handler created to be used for updating the song time
        handler = new Handler();

        init(); //temporary

        if(getIntent().getExtras() != null){
            player.load("file:///" +getIntent().getStringExtra(EXTRA_SONG_INDEX_PATH));
        }



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case R.id.load_song:

                initiateLoadSongActivity();


                break;
            default:

        }

        return true;
    }
    //http://stackoverflow.com/questions/7203668/how-permission-can-be-checked-at-runtime-without-throwing-securityexception
    //http://stackoverflow.com/questions/30549561/how-to-check-grants-permissions-at-run-time
    private boolean checkReadExternalPermission(){
        String permission = "android.permission.READ_EXTERNAL_STORAGE";
        int p = this.checkCallingOrSelfPermission(permission);
        return (p == PackageManager.PERMISSION_GRANTED);
    }

    private void initiateLoadSongActivity(){
        //https://developer.android.com/training/permissions/requesting.html
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {
                //request permission
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_READ_STORAGE_PERMISSION);
            }
        }

        if(checkReadExternalPermission()){
            //send off to activity which displays all tracks if we are allowed to acces storage
            Intent i = new Intent(this, MusicPicker.class);
            startActivityForResult(i, REQUEST_SELECT_MUSIC);
            overridePendingTransition(0, 0);
        }else{

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_STORAGE_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(this, R.string.storage_permission_denial, Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("Selected Song", "Code" + requestCode + ", " + resultCode + ", " + data.hasExtra(EXTRA_SONG_INDEX_PATH));
        switch(requestCode){
            case REQUEST_SELECT_MUSIC:
                //if(serviceConnection != null ){this.unbindService(serviceConnection);}

                //we get the data from selected track in previous acivity
                String songPath = data.getStringExtra(EXTRA_SONG_INDEX_PATH);
                player.stop();
                updatePlayStatus();
                player.load("file:///" + songPath);


                //
                Intent i = new Intent(this, MusicService.class);
                i.putExtra(EXTRA_SONG_INDEX_PATH, songPath);

                this.bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);
                Log.d("Main", "Service bound");

                break;
            default:

        }
    }

    void init(){
        nextButton.setVisibility(View.GONE);
        prevButton.setVisibility(View.GONE);
        time.setVisibility(View.GONE);

        //circularSlider.setStartAngle(0);

    }



    @Override
    protected void onResume() {
        //todo update progress
        Log.d("TuneMan", "Main Resumed");
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("TuneMan", "Main Destroyed");

        //when we kill the process, the service goes with it.
        if(serviceConnection!=null){
            Log.d("Service", "Service disconnected");

            unbindService(serviceConnection);
            serviceConnection = null;
        }
    }

    @Override
    protected void onPause() {
        Log.d("TuneMan", "Main Paused");

        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("TuneMan", "Main Started");
    }
}
