package devicroft.tuneman;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Created by m on 14-Nov-16.
 */

//https://developer.android.com/guide/topics/media/mediaplayer.html

public class MusicService extends IntentService {

    private static final String ACTION_PLAY = "devicroft.tuneman.PLAY";
    MP3Player player = null;
    private final IBinder binder = new MusicBinder();
    String currentMusicPath = null;
    NotificationManager nm;

    public class MusicBinder extends Binder {
        public MusicBinder() {
            super();
        }


    }

    void playPauseMusic() {
        if (player.getState() == MP3Player.MP3PlayerState.PAUSED) {
            Log.d("Bound service", "Pause pressed");

        } else {
            Log.d("Bound service", "Play pressed");

        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent.getAction().equals(ACTION_PLAY)){

        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("TuneMan", "Service bound");
        return null;
    }

    public MusicService(String name) {
        super(name);

    }

    public MusicService(){
        super("musicService");

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(intent.hasExtra(MainActivity.EXTRA_SONG_INDEX_PATH)){
            currentMusicPath = intent.getStringExtra(MainActivity.EXTRA_SONG_INDEX_PATH);
            player.load(currentMusicPath);
        }

    }


    private static final int NOTIF_MUSIC = 5;
    //https://stackoverflow.com/questions/18425108/how-should-i-do-from-notification-back-to-activity-without-new-intent
    @Override
    public void onCreate() {
        Log.d("TuneMan", "Service created");
        super.onCreate();
        Log.d("Notify", "Building Notification");

        String songTitle = "Music "; //  currentMusicPath.substring(currentMusicPath.lastIndexOf("/"), currentMusicPath.length());

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.icon)
                        .setContentTitle("TuneMan")
                        .setContentText(songTitle + " is playing");        //TODO song name here

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        //To return the file path of the current playing song
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP); //https://stackoverflow.com/questions/11551195/intent-from-notification-does-not-have-extras
        intent.putExtra(MainActivity.EXTRA_SONG_INDEX_PATH, currentMusicPath);
        if(player != null){intent.putExtra(MainActivity.EXTRA_SONG_PROGRESS, player.getProgress());}

        PendingIntent pendingIntent= PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        nm = (NotificationManager)  getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTIF_MUSIC, builder.build());


    }

    @Override
    public void onDestroy() {
        nm.cancelAll();
        super.onDestroy();
    }
}
