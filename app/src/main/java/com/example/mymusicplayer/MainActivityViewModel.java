package com.example.mymusicplayer;

import android.Manifest;
import android.app.Application;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class MainActivityViewModel extends ViewModel {
    volatile int songDuration=0, songProgress=0;
    volatile String playingSongInfo = "NOT_PLAYING"; // NOT_PLAYING | PLAYING | PAUSE
    volatile String currentPlayingSongName = "Choose a Song";
    Thread t = new Thread("UI thread");

    public MainActivityViewModel() {
        System.out.println("ViewModel created");
    }

    public int getSongDuration() {
        return songDuration;
    }

    public int getSongProgress() {
        return songProgress;
    }

    public String getPlayingSongInfo() {
        return playingSongInfo;
    }

    public String getCurrentPlayingSongName() {
        return currentPlayingSongName;
    }

    public Thread getT() {
        return t;
    }

    public Thread setT(Thread t) {
        this.t = t;
        return this.t;
    }

    public void setCurrentPlayingSongName(String currentPlayingSongName) {
        this.currentPlayingSongName = currentPlayingSongName;
    }

    public void setSongDuration(int songDuration) {
        this.songDuration = songDuration;
    }

    public void setSongProgress(int songProgress) {
        this.songProgress = songProgress;
    }

    public void setPlayingSongInfo(String playingSongInfo) {
        this.playingSongInfo = playingSongInfo;
    }


}
