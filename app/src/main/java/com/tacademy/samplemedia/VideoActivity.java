package com.tacademy.samplemedia;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoActivity extends AppCompatActivity {

    VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        videoView = (VideoView)findViewById(R.id.video_view);

        MediaController mController = new MediaController(this);
        videoView.setMediaController(mController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_video, menu);
        return true;
    }

    private final static int RC_VIDEO_LIST = 0;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_video_list) {
            Intent intent = new Intent(VideoActivity.this, VideoListActivity.class);
            startActivityForResult(intent, RC_VIDEO_LIST);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RC_VIDEO_LIST) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();
                videoView.setVideoURI(uri);
                videoView.start();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
