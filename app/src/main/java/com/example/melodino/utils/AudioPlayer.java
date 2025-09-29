package com.example.melodino.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.widget.Toast;

public class AudioPlayer {

    private MediaPlayer mediaPlayer;
    private Handler handler;
    private Context context;
    private OnPlaybackListener listener;

    public interface OnPlaybackListener {
        void onPlaybackStarted();
        void onPlaybackStopped();
        void onPlaybackStateChanged(boolean isPlaying);
    }

    public AudioPlayer(Context context, int audioResourceId) {
        this.context = context;
        this.handler = new Handler();
        this.mediaPlayer = MediaPlayer.create(context, audioResourceId);
    }

    public void setOnPlaybackListener(OnPlaybackListener listener) {
        this.listener = listener;
    }

    public void playFragment(int durationMillis) {
        if (mediaPlayer != null) {
            // Stop if already playing
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                mediaPlayer.seekTo(0);
            }

            // Start playing
            mediaPlayer.start();

            if (listener != null) {
                listener.onPlaybackStarted();
                listener.onPlaybackStateChanged(true);
            }

            // Stop after specified duration
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopPlayback();
                }
            }, durationMillis);
        }
    }

    private void stopPlayback() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);

            if (listener != null) {
                listener.onPlaybackStopped();
                listener.onPlaybackStateChanged(false);
            }
        }
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacksAndMessages(null);
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }
}