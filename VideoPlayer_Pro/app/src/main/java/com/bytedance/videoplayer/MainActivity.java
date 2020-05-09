package com.bytedance.videoplayer;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.text.style.UpdateAppearance;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;

import java.sql.Time;
import java.util.Formatter;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    static private int UpDate = 0;
    private VideoView mVideoView;
    private ImageView mMediaActions;
    private TextView mMediaTime;
    private TextView mMediaTotalTime;
    private SeekBar mMediaSeekBar;
    private ImageView mMediaFullScreen;
    boolean isFull=false, isvisiable=true;
    int width, height;
    StringBuilder mFormatBuilder = new StringBuilder();
    Formatter mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        width = dm.widthPixels;
        height = dm.heightPixels;

        mVideoView = findViewById(R.id.videoView);
        mMediaActions = findViewById(R.id.media_actions);
        mMediaTime = findViewById(R.id.media_time);
        mMediaTotalTime = findViewById(R.id.media_total_time);
        mMediaSeekBar = findViewById(R.id.media_seekbar);
        mMediaFullScreen = findViewById(R.id.media_full_screen);

        Uri uri = null;
        uri = getIntent().getData();
        if(uri == null)
            mVideoView.setVideoPath("android.resource://" + this.getPackageName() + "/" + R.raw.bytedance);
        else {
            //加载网络url
            mVideoView.setVideoURI(uri);
            mMediaActions.setImageResource(R.drawable.pause);
            mVideoView.start();
            mHandler.sendEmptyMessage(UpDate);
        }

        mMediaActions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mVideoView.isPlaying()){
                    mMediaActions.setImageResource(R.drawable.play);
                    mVideoView.pause();
                    mHandler.removeMessages(UpDate);
                }else {
                    mMediaActions.setImageResource(R.drawable.pause);
                    mVideoView.start();
                    mHandler.sendEmptyMessage(UpDate);
                }
            }
        });

        //播放器的进度条监听
        mMediaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                updateTime(mMediaTime,progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mHandler.removeMessages(UpDate);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                mVideoView.seekTo(progress);
                mHandler.sendEmptyMessage(UpDate);
            }
        });

        //设置全屏播放
        mMediaFullScreen.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SourceLockedOrientationActivity")
            @Override
            public void onClick(View view) {
                if (isFull){
                    //此时是全屏
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    isFull = false;
                }else {
                    //此时是半屏
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    isFull = true;
                }
            }
        });

        mVideoView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(isvisiable) {
                    LinearLayout slide = findViewById(R.id.fr);
                    slide.setVisibility(View.GONE);
                    isvisiable = false;
                }
                else {
                    LinearLayout slide = findViewById(R.id.fr);
                    slide.setVisibility(View.VISIBLE);
                    isvisiable = true;
                }
            }
        });
    }



    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == UpDate){
                //当前时间
                int currentPosition = mVideoView.getCurrentPosition();
                //总时间
                int duration = mVideoView.getDuration();

                mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mMediaActions.setImageResource(R.drawable.play);
                    }
                });
                //设置时间
                updateTime(mMediaTime,currentPosition);
                updateTime(mMediaTotalTime,duration);
                //设置进度
                mMediaSeekBar.setMax(duration);
                mMediaSeekBar.setProgress(currentPosition);

                mHandler.sendEmptyMessageDelayed(UpDate,500);
            }
        }
    };

    void updateTime(TextView mMediaTime, int time) {
        int totalSeconds = time / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            mMediaTime.setText(mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString());
        } else {
            mMediaTime.setText(mFormatter.format("%02d:%02d", minutes, seconds).toString());
        }
    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        //当为横屏时候
//        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
//            setConfigWh(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
//            isFull = true;
//
//            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//
//        }else{
//            //当为竖屏时候
//            setConfigWh(ViewGroup.LayoutParams.MATCH_PARENT, DensityUtil.dp2px(this,240));
//            isFull = false;
//
//            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//        }
//    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            initHalfFullState(true);
            Log.i("liu", String.valueOf(isFull));
            isFull = true;


        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            initHalfFullState(false);
            isFull = false;
            Log.i("liu", String.valueOf(isFull));

        }
    }


    private void initHalfFullState(boolean isFull){

        showFullScreen(isFull);
    }

    //---------videoview fullscreen---------
    private void showFullScreen(boolean isFullScreen){
        if(isFullScreen){
//            //不显示程序的标题栏
            hideNavigationBar();
        }else{
            showNavigationBar();
        }
    }

    private void showNavigationBar(){
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    public void hideNavigationBar() {
        int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN; // hide status bar

        getWindow().getDecorView().setSystemUiVisibility(uiFlags);
    }

}
