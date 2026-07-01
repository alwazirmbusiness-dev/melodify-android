package com.melodifyverse.nancyajram;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

public class PlayerManager {

    private static MediaPlayer mp;
    private static int currentPos = -1;
    private static String currentAsset = null;
    private static String currentSongTitle = ""; // Add this
    private static boolean hasCompleted = false; // Add this flag


    // ===============================
    // Song identity
    // ===============================
    public static boolean isSameSong(String assetName) {
        return assetName != null && assetName.equals(currentAsset);
    }

    // ===============================
    // Completion callback support
    // ===============================
    public interface OnCompletionCallback {
        void onComplete();
    }

    private static OnCompletionCallback completionCallback;

    public static void setOnCompletionListener(OnCompletionCallback callback) {
        completionCallback = callback;
    }

    // ===============================
    // Core playback
    // ===============================
    public static void play(Context c, String asset, int pos, String title) { // Add title
        try {
            if (mp == null) {
                mp = new MediaPlayer();
            } else {
                mp.reset();
            }

            AssetFileDescriptor afd = c.getAssets().openFd(asset);
            mp.setDataSource(
                    afd.getFileDescriptor(),
                    afd.getStartOffset(),
                    afd.getLength()
            );
            afd.close();

            mp.prepare();
            mp.start();

            currentAsset = asset;
            currentPos = pos;
            currentSongTitle = title; // Set title
            hasCompleted = false; // Reset the flag on new song


            mp.setOnCompletionListener(mediaPlayer -> {
                hasCompleted = true; // Set flag on completion
                if (completionCallback != null) {
                    completionCallback.onComplete();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===============================
    // Controls
    // ===============================
    public static void toggle() {
        if (mp == null) return;

        if (mp.isPlaying()) mp.pause();
        else mp.start();
    }

    public static void pause() {
        if (mp != null && mp.isPlaying()) mp.pause();
    }

    public static void resume() {
        if (mp != null && !mp.isPlaying()) mp.start();
    }

    public static void stop() {
        if (mp != null) {
            try {
                if (mp.isPlaying()) mp.stop();
                mp.reset();
                mp.release(); // Release native resources
            } catch (IllegalStateException ignored) {}

            mp = null; // Null out so isPlaying() returns false cleanly
            currentAsset = null;
            currentPos = -1;
            currentSongTitle = "";
        }
    }

    // ===============================
    // State helpers
    // ===============================
    public static boolean isPlaying() {
        return mp != null && mp.isPlaying();
    }

    public static int getDuration() {
        return mp != null ? mp.getDuration() : 0;
    }

    public static int getCurrentPosition() {
        return mp != null ? mp.getCurrentPosition() : 0;
    }

    public static void seekTo(int ms) {
        if (mp != null) mp.seekTo(ms);
    }

    public static int getCurrentPos() {
        return currentPos;
    }

    public static String getCurrentAsset() {
        return currentAsset;
    }

    public static String getCurrentSongTitle() { // Add this method
        return currentSongTitle;
    }

    public static boolean hasCompleted() {
        return hasCompleted;
    }

    public static void setHasCompleted(boolean completed) {
        hasCompleted = completed;
    }

    public static MediaPlayer getMediaPlayer() {
        return mp;
    }
}
