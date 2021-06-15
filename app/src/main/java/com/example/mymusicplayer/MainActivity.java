package com.example.mymusicplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.app.ActivityManager;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements Runnable {
    static final int PERMISSION_CODE = 69;
    static final String READ_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE;
    ArrayList<String> musicFilesList;
    TextView songNameTextView;
    SeekBar songSeekBar;
    Button playPauseButton;
    ListView songsList;
    int playingSongPosition;
    MediaPlayer mediaPlayer;
    Boolean isTouchingSeekBar;
    volatile TextView songPlayedTimeText;
    volatile int songDuration, songProgress;
    volatile String playingSongInfo; // NOT_PLAYING | PLAYING | PAUSE
    Thread t;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkForUserPermissions();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void checkForUserPermissions(){
        if(ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE_PERMISSION) != PackageManager.PERMISSION_GRANTED){
            // Check if the user has been asked about this permission already and denied
            if(shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE_PERMISSION)){
                // Show a message explaining why you need to give this permission.
            }
            requestPermissions(new String[]{READ_EXTERNAL_STORAGE_PERMISSION}, PERMISSION_CODE);
        } else {
            startApp();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Callback with the request from calling requestPermissions(...)
        if(requestCode == PERMISSION_CODE){
            if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
                startApp();
            } else {
                Toast.makeText(this, "You denined the permissions, exiting the app", Toast.LENGTH_SHORT).show();
                ((ActivityManager) (Objects.requireNonNull(this.getSystemService(ACTIVITY_SERVICE)))).clearApplicationUserData();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle saveState){
        super.onSaveInstanceState(saveState);
//        Toast.makeText(this, "SAVING---1", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onRestoreInstanceState(Bundle saveState){
        super.onSaveInstanceState(saveState);
//        Toast.makeText(this, "RESTORING---2", Toast.LENGTH_SHORT).show();
    }

    void startApp(){
        songNameTextView = findViewById(R.id.SongName);
        songSeekBar = findViewById(R.id.songSeekBar);
        playPauseButton = findViewById(R.id.BtnPlayPause);
        songsList = findViewById(R.id.SongList);
        findViewById(R.id.SongName).setSelected(true);
        songPlayedTimeText = findViewById(R.id.SongPlayedTime);

        musicFilesList = new ArrayList<>();
        mediaPlayer = new MediaPlayer();
        playingSongInfo = "NOT_PLAYING";
        songProgress = 0;
        isTouchingSeekBar = false;

        searchForMusicFiles();
        String[] musicFiles = convertArrayList_to_StringArray(musicFilesList);
        ArrayAdapter<String> songsAdapter = new ArrayAdapter<String>(this, R.layout.individual_song_layout, musicFiles);
        songsList.setAdapter(songsAdapter);

        addListenersToComponents();
    }

    private String[] convertArrayList_to_StringArray(ArrayList<String> arrList){
        String[] strArr = new String[arrList.size()];
        Collections.sort(arrList);
        for(int i=0;i<arrList.size();i++){
            String path = (String) arrList.get(i);
            strArr[i] = path.substring(path.lastIndexOf("/")+1);
        }
        return strArr;
    }

    void searchForMusicFiles(){
        String downloads_dir_location = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"";
        String music_dir_location = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)+"";
        String[] musicDirs = {downloads_dir_location};

        for(String musicFile: musicDirs){
            final File musicDir = new File(musicFile);
            if(!musicDir.exists()){continue;}

            final File[] musicFiles = musicDir.listFiles();
            if(musicFiles != null){
                for(File file: musicFiles){
                    String filePath = file.getAbsolutePath();
                    if(filePath.endsWith(".mp3") || filePath.endsWith(".m4a") || filePath.endsWith(".wav")){
                        musicFilesList.add(filePath);
                    }
                }
            } else {
                Toast.makeText(this, "NO files found in "+musicFile, Toast.LENGTH_SHORT).show();
            }
        }
    }

    void setTimeOfTextView(int time, TextView textview){
        int tempSecs = time/1000;
        String minutes = (tempSecs/60)+"";
        String seconds = (tempSecs%60)+"";
        if((Integer.parseInt(seconds)/10)==0){seconds="0"+seconds;}
        String displayTime = minutes+":"+seconds;
        textview.setText(displayTime);
    }

    void playSong(int position){
        String path = musicFilesList.get(position);
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            mediaPlayer.start();
            setTimeOfTextView(mediaPlayer.getDuration(), findViewById(R.id.SongTotalTime));
        } catch (Exception e) {
            e.printStackTrace();
        }
        String sName = path.substring(path.lastIndexOf("/")+1);
        songNameTextView.setText(sName);
        playPauseButton.setBackgroundResource(R.drawable.song_pause_icon);
        songSeekBar.setMax(mediaPlayer.getDuration());
        playingSongPosition = position;
        songDuration =  mediaPlayer.getDuration();
        playingSongPosition = position;
    }

    void pauseSong(){
        mediaPlayer.pause();
        playingSongInfo="PAUSE";
        playPauseButton.setBackgroundResource(R.drawable.song_play_icon);
    }

    void resetSong(int newPosition){
        mediaPlayer.stop();
        mediaPlayer.reset();
        resetUI();
        playSong(newPosition);
    }

    void resetUI(){
        setTimeOfTextView(0, songPlayedTimeText);
        songSeekBar.setProgress(0);
        songProgress = 0;
    }

    public void createThread(String name){
        t = new Thread(this, name);
        t.start();
    }

    public void run(){
        while(true){
            if((playingSongInfo.equals("PLAYING")) && (songProgress < songDuration)){
                songProgress += 150;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        songSeekBar.setProgress(songProgress);
                        setTimeOfTextView(songProgress, songPlayedTimeText);
                    }
                });
                try{
                    Thread.sleep(150);
                }catch (Exception e){Log.d("Info", "Error in the run method of the thread");}
            }
        }
    }

    void addListenersToComponents(){
        // Listener for the song list
        songsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(playingSongInfo){
                    case "NOT_PLAYING":
                        playSong(position);
                        createThread("UI updating thread");
                        playingSongInfo="PLAYING";
                        break;
                    case "PLAYING":
                        if(position == playingSongPosition){return;}
                        resetSong(position);
                        break;
                    case "PAUSE":
                        if(position == playingSongPosition){
                            mediaPlayer.start();
                        } else {
                            // If another song is pressed when current is paused
                            resetSong(position);
                        }
                        playingSongInfo = "PLAYING";
                }
            }
        });

        // Listener for the play and pause button.
        playPauseButton.setOnClickListener(v -> {
            switch(playingSongInfo){
                case "NOT_PLAYING":
                    Random r = new Random();
                    int rndNum = r.nextInt(musicFilesList.size());
                    playSong(rndNum);
                    createThread("UI updating thread");
                    playingSongInfo="PLAYING";
                    break;
                case "PLAYING":
                    pauseSong();
                    break;
                case "PAUSE":
                    mediaPlayer.start();
                    playPauseButton.setBackgroundResource(R.drawable.song_pause_icon);
                    playingSongInfo = "PLAYING";
            }
        });

        mediaPlayer.setOnCompletionListener(v -> {
                mediaPlayer.reset();
                if(++playingSongPosition>=musicFilesList.size()){playingSongPosition=0;}
                resetSong(playingSongPosition);
        });

        songSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int song_progress = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                song_progress = progress;
                Log.d("Report", "Changed");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(playingSongInfo=="NOT_PLAYING"){return ;}
                mediaPlayer.seekTo(song_progress);
                songProgress = song_progress;
                Log.d("Report", "Stopped Tracking Touching");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d("Report", "Started Traking Toughing");
            }
        });

        findViewById(R.id.BtnPlayNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!playingSongInfo.equals("NOT_PLAYING")){
                    resetSong(++playingSongPosition);
                }
            }
        });

        findViewById(R.id.BtnPlayPrevious).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // To check whether the button is pressed within 2 seconds
                if(playingSongInfo.equals("NOT_PLAYING")) {
                    return;
                }
                if(songProgress >= 2000) {
                    resetSong(playingSongPosition);
                } else {
                    resetSong(--playingSongPosition);
                }
            }
        });

    }
}
