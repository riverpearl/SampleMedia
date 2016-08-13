package com.tacademy.samplemedia;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import java.io.IOException;

public class PlayerActivity extends AppCompatActivity {

    SeekBar musicView;
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

    boolean isSeeking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        musicView = (SeekBar)findViewById(R.id.seek_music);
        mPlayer = MediaPlayer.create(this, R.raw.run_and_run);
        pState = PlayState.PREPARED;

        musicView.setMax(mPlayer.getDuration());
        musicView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = -1;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    this.progress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                progress = -1;
                isSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (progress != -1 && pState == PlayState.STARTED)
                    mPlayer.seekTo(progress);

                isSeeking = false;
            }
        });

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
            mPlayer.seekTo(musicView.getProgress());
            mPlayer.start();
            pState = PlayState.STARTED;
            mHandler.post(progressRunnable);
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
            musicView.setProgress(0);
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

    Handler mHandler = new Handler(Looper.getMainLooper());

    Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (pState == PlayState.STARTED) {
                if (!isSeeking) {
                    int position = mPlayer.getCurrentPosition();
                    musicView.setProgress(position);
                }

                mHandler.postDelayed(this, 100);
            }
        }
    };
}
