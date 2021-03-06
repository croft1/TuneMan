package devicroft.tuneman;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

/**
 * Created by m on 13-Nov-16.
 */


public class MP3Player {

    protected MediaPlayer mediaPlayer;
    protected MP3PlayerState state;
    protected String filePath = null;

    public enum MP3PlayerState {
        ERROR,
        PLAYING,
        PAUSED,
        STOPPED
    }

    public MP3Player() {
        this.state = MP3PlayerState.STOPPED;
    }

    public MP3PlayerState getState() {
        return this.state;
    }

    public void load(String filePath) {
        this.filePath = filePath;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try{
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
        } catch (IOException e) {
            Log.e("MP3Player", e.toString());
            e.printStackTrace();
            this.state = MP3PlayerState.ERROR;
            return;
        } catch (IllegalArgumentException e) {
            Log.e("MP3Player", e.toString());
            e.printStackTrace();
            this.state = MP3PlayerState.ERROR;
            return;
        }

        this.state = MP3PlayerState.PLAYING;
        mediaPlayer.start();
    }

    public String getFilePath() {
        return this.filePath;
    }

    public int getProgress() {
        if(mediaPlayer!=null) {
            if(this.state == MP3PlayerState.PAUSED || this.state == MP3PlayerState.PLAYING)
                return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public void setProgress() {
        if (mediaPlayer != null) {
            if (this.state == MP3PlayerState.PAUSED || this.state == MP3PlayerState.PLAYING) {
                //todo change the time of the audio
            }
        }

    }

    public void play() {
        if(this.state == MP3PlayerState.PAUSED) {
            mediaPlayer.start();
            this.state = MP3PlayerState.PLAYING;
            mediaPlayer.setLooping(true);
        }
    }

    public void pause() {
        if(this.state == MP3PlayerState.PLAYING) {
            mediaPlayer.pause();
            state = MP3PlayerState.PAUSED;
        }
    }

    public void stop() {
        if(mediaPlayer!=null) {
            if(mediaPlayer.isPlaying())
                mediaPlayer.stop();
            state = MP3PlayerState.STOPPED;
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        filePath = null;
    }

    public boolean isLoaded(){ return (filePath != null) ? true : false; }

}