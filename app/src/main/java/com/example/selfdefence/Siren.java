package com.example.selfdefence;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Siren extends AppCompatActivity implements View.OnClickListener{


    Button play,pause,stop;
    MediaPlayer mediaPlayer;
    int pauseCurrentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_siren);

        play=(Button)findViewById(R.id.btnplay);
        pause=(Button)findViewById(R.id.btnpause);
        stop=(Button)findViewById(R.id.btnstop);

        play.setOnClickListener(this);
        pause.setOnClickListener(this);
        stop.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.btnplay:
                if(mediaPlayer == null){
                    mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.police_siren);
                    mediaPlayer.start();
                }
                else if (!mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(pauseCurrentPosition);  //strt it jahn sey pause kiya tha
                    mediaPlayer.start();
                }
                break;

            case R.id.btnpause:
                if (mediaPlayer != null){
                    mediaPlayer.pause();
                    pauseCurrentPosition=mediaPlayer.getCurrentPosition();
                }
                break;

            case R.id.btnstop:
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer = null;
                }
                break;
        }
    }
}