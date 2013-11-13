package com.phonehalo.itemtracker.helper;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import com.phonehalo.ble.util.Log;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class AlertHelperMediaPlayer implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private final String LOG_TAG = "AlertHelperMediaPlayer";

    private MediaPlayer mediaPlayer;

    private Context context;
    private boolean prepareComplete;
    private boolean playSoundOnPrepared;
    private int volume;
    private boolean vibrate;
    private int durationSeconds;
    private MediaPlayer mMediaPlayer;

    public void setup(Context context, Uri audioUri, int volume, int durationSeconds, boolean vibrate) {
        this.context = context;
        this.vibrate = vibrate;
        this.durationSeconds = durationSeconds;

        setupMediaPlayer(audioUri, volume);
    }

    private void setupMediaPlayer(Uri audioUri, int volume) {
        this.volume = volume;
        Log.v(LOG_TAG, "alertPhone: " + audioUri);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
        try {
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setDataSource(context, audioUri);
            mediaPlayer.setLooping(true);
            mediaPlayer.prepareAsync();
            mMediaPlayer = mediaPlayer;
        }
        catch (IOException e) {
            Log.e(LOG_TAG, "MediaPlayer error playing audio", e);
        }
    }

    public void stop()
    {
        try{
        mMediaPlayer.stop();
        }catch (NullPointerException e)
        {
            e.printStackTrace();
        }catch (IllegalStateException e)
        {
            e.printStackTrace();
        }
    }

    public void play() {
        Log.v(LOG_TAG, "play");
        if (mediaPlayer != null && !prepareComplete) {
            Log.v(LOG_TAG, "MediaPlayer not prepared");
            playSoundOnPrepared = true;
        } else if (mediaPlayer != null) {
            Log.v(LOG_TAG, "about to play sound");
            playSoundWithVolumeManagement();
        } else {
            Log.e(LOG_TAG, "MediaPlayer is null");
        }
    }

    private void playSoundWithVolumeManagement() {
        final int previousVolume = setVolume(context, AudioManager.STREAM_ALARM, volume);
        mediaPlayer.start();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try{
                mediaPlayer.stop();
                mediaPlayer.release();
                }catch (IllegalStateException e)
                {
                    e.printStackTrace();
                }
                setVolume(context, AudioManager.STREAM_ALARM, previousVolume);

            }
        }, durationSeconds * 1000);
    }

    /**
     * @param audioStream the AudioManager.STREAM_* constant of the stream
     * @param volume the Android volume index for the stream
     * @return previous volume index for that stream
     */
    private int setVolume(Context context, int audioStream, int volume) {
        Log.v(LOG_TAG, "setVolume: " + volume);
        final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int previousVolume = audioManager.getStreamVolume(audioStream);
        audioManager.setStreamVolume(audioStream, volume, 0);
        Log.v(LOG_TAG, "Previous volume was: " + previousVolume);
        return previousVolume;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.v(LOG_TAG, "audio complete");
        mp.release();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(LOG_TAG, "MediaPlayer Error: " + what + ", extra: " + extra);
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.v(LOG_TAG, "MediaPlayer prepared");
        this.prepareComplete = true;
        mp.setVolume(volume, volume);
        if (playSoundOnPrepared) {
            playSoundWithVolumeManagement();
        }
    }
}
