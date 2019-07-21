package com.trukk.media;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;

public class AudioRecorder {
    private static final String LOG_TAG = "AudioRecorder";

    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    public static boolean isRecording;
    public static boolean isPlaying;
    public boolean isRecorded = false;

    public AudioRecorder(){
        mediaRecorder = null;
        mediaPlayer = null;
    }

    public void startRecording(String fileName) {
        Log.d(LOG_TAG, "Starting recording");
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile(fileName);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            mediaRecorder.prepare();
        } catch (IOException e){
            Log.e(LOG_TAG, "prepare() failed-->"+e.getMessage());
        }
        mediaRecorder.start();
        isRecording = true;
    }

    public void stopRecording() {
        if (!isRecording){
            return;
        }
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        isRecording = false;
        isRecorded = true;
    }

    public void startPlaying(String fileName){
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(fileName);
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying = true;
        } catch (IOException e){
            Log.e(LOG_TAG, "prepare() failed-->"+e.getMessage());
        }
    }

    public void stopPlaying() {
        if (!isPlaying){
            return;
        }
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        isPlaying = false;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }
}
