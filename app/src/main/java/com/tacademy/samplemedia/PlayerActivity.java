package com.tacademy.samplemedia;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;

public class PlayerActivity extends AppCompatActivity {

    SeekBar musicView, volumeView;
    CheckBox muteView;

    MediaPlayer mPlayer;
    PlayState pState;
    AudioManager aManager;

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
    float currentVolume = 1.0f;

    private static final int RC_MUSIC_LIST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        musicView = (SeekBar)findViewById(R.id.seek_music);
        volumeView = (SeekBar)findViewById(R.id.seek_volume);
        muteView = (CheckBox)findViewById(R.id.cbox_mute);

        mPlayer = MediaPlayer.create(this, R.raw.run_and_run);
        pState = PlayState.PREPARED;
        aManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

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

        int max = aManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int current = aManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        volumeView.setMax(max);
        volumeView.setProgress(current);
        volumeView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    aManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        muteView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    mHandler.removeCallbacks(volumeUp);
                    mHandler.post(volumeDown);
                } else {
                    mHandler.removeCallbacks(volumeDown);
                    mHandler.post(volumeUp);
                }
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

        btn = (Button)findViewById(R.id.btn_get);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PlayerActivity.this, MusicListActivity.class);
                startActivityForResult(intent, RC_MUSIC_LIST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_MUSIC_LIST) {
            if (resultCode == Activity.RESULT_OK) {
                mPlayer.reset();
                pState = PlayState.IDLE;
                Uri uri = data.getData();

                try {
                    mPlayer.setDataSource(this, uri);
                    pState = PlayState.INITIALIED;

                    mPlayer.prepare();
                    pState = PlayState.PREPARED;

                    musicView.setMax(mPlayer.getDuration());
                    musicView.setProgress(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
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

    Runnable volumeUp = new Runnable() {
        @Override
        public void run() {
            if (currentVolume < 1.0f) {
                mPlayer.setVolume(currentVolume, currentVolume);
                currentVolume += 0.1f;
                mHandler.postDelayed(this, 100);
            } else {
                currentVolume = 1.0f;
                mPlayer.setVolume(currentVolume, currentVolume);
            }
        }
    };

    Runnable volumeDown = new Runnable() {
        @Override
        public void run() {
            if (currentVolume > 0) {
                mPlayer.setVolume(currentVolume, currentVolume);
                currentVolume -= 0.1f;
                mHandler.postDelayed(this, 100);
            } else {
                currentVolume = 0;
                mPlayer.setVolume(currentVolume, currentVolume);
            }
        }
    };
}
