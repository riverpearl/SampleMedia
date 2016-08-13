package com.tacademy.samplemedia;

import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

public class PlayerActivity extends AppCompatActivity {

    MediaPlayer mPlayer;
    PlayState pState;

    enum PlayState {
        IDLE,
        INITIALIED,
        PREPARED,
        STARTED,
        PAUSED,
        STOPPED,
        ERROR,
        RELEASED
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        mPlayer = MediaPlayer.create(this, R.raw.run_and_run);
        pState = PlayState.PREPARED;

        Button btn = (Button)findViewById(R.id.btn_start);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play();
            }
        });

        btn = (Button)findViewById(R.id.btn_pause);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pause();
            }
        });

        btn = (Button)findViewById(R.id.btn_stop);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stop();
            }
        });
    }

    private void play() {
        if (pState == PlayState.INITIALIED || pState == PlayState.STOPPED) {
            try {
                mPlayer.prepare();
                pState = PlayState.PREPARED;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        if (pState == PlayState.PREPARED || pState == PlayState.PAUSED) {
            mPlayer.start();
            pState = PlayState.STARTED;
        }
    }

    private void pause() {
        if (pState == PlayState.STARTED) {
            mPlayer.pause();
            pState = PlayState.PAUSED;
        }
    }

    private void stop() {
        if (pState == PlayState.STARTED || pState == PlayState.PAUSED) {
            mPlayer.stop();
            pState = PlayState.STOPPED;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlayer.release();
        mPlayer = null;
        pState = PlayState.RELEASED;
    }
}
