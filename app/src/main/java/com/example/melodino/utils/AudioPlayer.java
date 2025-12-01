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
    private boolean isPrepared = false;

    public interface OnPlaybackListener {
        void onPlaybackStarted();

        void onPlaybackStopped();

        void onPlaybackStateChanged(boolean isPlaying);

        void onAudioReady();

        void onAudioError(String message);
    }

    public AudioPlayer(Context context, int audioResourceId) {
        this.context = context;
        this.handler = new Handler();
        this.mediaPlayer = MediaPlayer.create(context, audioResourceId);
        this.isPrepared = true; // Local resources are usually ready immediately
    }

    public AudioPlayer(Context context, String url) {
        this.context = context;
        this.handler = new Handler();
        this.mediaPlayer = new MediaPlayer();
        this.isPrepared = false;
        try {
            android.media.AudioAttributes audioAttributes = new android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            mediaPlayer.setAudioAttributes(audioAttributes);

            java.util.Map<String, String> headers = new java.util.HashMap<>();
            headers.put("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile; rv:89.0) Gecko/89.0 Firefox/89.0");

            mediaPlayer.setDataSource(context, android.net.Uri.parse(url), headers);

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                android.util.Log.e("Melodino", "MediaPlayer Error: what=" + what + ", extra=" + extra);
                if (listener != null) {
                    listener.onAudioError("MediaPlayer Error: " + what);
                }
                return true; // Handled
            });

            mediaPlayer.setOnPreparedListener(mp -> {
                android.util.Log.d("Melodino", "MediaPlayer Prepared");
                isPrepared = true;
                if (listener != null) {
                    listener.onAudioReady();
                }
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                // When playback completes naturally (e.g. song ends before timeout)
                // Reset to start so user can play again
                mp.seekTo(0);

                // Notify listener that playback stopped
                if (listener != null) {
                    listener.onPlaybackStopped();
                    listener.onPlaybackStateChanged(false);
                }
            });

            mediaPlayer.prepareAsync(); // Asynchronous prepare to avoid blocking UI thread
        } catch (Exception e) {
            android.util.Log.e("Melodino", "AudioPlayer Exception: " + e.getMessage());
            e.printStackTrace();
            // We can't call listener here easily as it's not set yet, but the caller should
            // handle the exception if they were calling this directly.
            // However, since this is a constructor, we can't really report back via
            // listener yet.
        }
    }

    public void setOnPlaybackListener(OnPlaybackListener listener) {
        this.listener = listener;
        // If already prepared when listener is set, notify immediately
        if (isPrepared && listener != null) {
            listener.onAudioReady();
        }
    }

    public void playFragment(int durationMillis) {
        if (mediaPlayer != null) {
            if (!isPrepared) {
                Toast.makeText(context, "Audio loading...", Toast.LENGTH_SHORT).show();
                return;
            }

            // Cancel any pending stop commands
            handler.removeCallbacksAndMessages(null);

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

    public void stopPlayback() {
        // Cancel any pending stop commands
        handler.removeCallbacksAndMessages(null);

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