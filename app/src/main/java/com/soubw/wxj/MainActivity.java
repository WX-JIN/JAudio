package com.soubw.wxj;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.soubw.jaudio.JAudioStatus;
import com.soubw.jaudio.JAudioView;
import com.soubw.other.CheapSoundFile;
import com.soubw.other.JWaveFormView;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //private File file = new File(Environment.getExternalStorageDirectory() + "/abc.wav");
    //private File file = new File(Environment.getExternalStorageDirectory() + "/abb.mp3");
    private File file = new File(Environment.getExternalStorageDirectory() + "/recordtest1.wav");
    //private File file = new File(Environment.getExternalStorageDirectory() + "/recordtest1.mp3");
    //private JAudioFile jAudioFile;
    private CheapSoundFile cheapSoundFile;
    private MediaPlayer mediaPlayer;
    private com.soubw.jaudio.JAudioView jAudioView;
    private android.widget.Button btnPlay;

    private boolean isFinishLoadFile = false;
    private JAudioView.OnJAudioViewListener onJAudioViewListener;

    private JWaveFormView jWaveFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.btnPlay = (Button) findViewById(R.id.btnPlay);
        this.jWaveFormView = (JWaveFormView) findViewById(R.id.jWaveFormView);
        this.btnPlay.setOnClickListener(this);
        this.jAudioView = (JAudioView) findViewById(R.id.jAudioView);
        loadJAudioView();

        onJAudioViewListener = new JAudioView.OnJAudioViewListener() {
            @Override
            public void finishLoadFile() {
                isFinishLoadFile = true;
            }
        };
        jAudioView.setOnJAudioViewListener(onJAudioViewListener);
    }

    private void loadJAudioView() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
/*                    jAudioFile = JAudioFile.create(file.getAbsolutePath(),null);

                    if (jAudioFile == null) {
                        return;
                    }
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(file.getAbsolutePath());
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.prepare();
                    Runnable runnable = new Runnable() {
                        public void run() {
                            jAudioView.setAudioFile(jAudioFile);
                        }
                    };*/
                    cheapSoundFile = CheapSoundFile.create(file.getAbsolutePath(), null);
                    if (cheapSoundFile == null) {
                        return;
                    }

                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(file.getAbsolutePath());
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.prepare();
                    Runnable runnable = new Runnable() {
                        public void run() {
                            Toast.makeText(MainActivity.this, "声道:" + cheapSoundFile.getChannels(), Toast.LENGTH_SHORT).show();
                            jAudioView.setAudioFile(cheapSoundFile);
                        }
                    };
                    MainActivity.this.runOnUiThread(runnable);
                }catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }).start();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnPlay:
                if (!isFinishLoadFile){
                    Toast.makeText(this, "正在载入音频，请稍等！", Toast.LENGTH_SHORT).show();
                    return;
                }
                playAndPause();
                break;
        }
    }


    private int mPlayStartMsec;
    private int mPlayEndMsec;
    Handler updateTime = new Handler() {
        public void handleMessage(Message msg) {
            updateDisplay();
            if (jAudioView.getJAudioStatus() == JAudioStatus.NORMAL)
                updateTime.sendMessageDelayed(new Message(), 10);
        }
    };

    private void playAndPause() {
        jWaveFormViewHandler.sendMessage(new Message());


/*        if (mediaPlayer == null)
            return;
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            jAudioView.setJAudioStatus(JAudioStatus.PAUSE);
            btnPlay.setText("播放");
        }else if (jAudioView.getJAudioStatus() == JAudioStatus.PAUSE){
            int now = mediaPlayer.getCurrentPosition();
            mediaPlayer.start();
            mediaPlayer.seekTo(now);
            jAudioView.setJAudioStatus(JAudioStatus.NORMAL);
            btnPlay.setText("暂停");
            Message msg = new Message();
            updateTime.sendMessage(msg);
        }else {
            jAudioView.setJAudioStatus(JAudioStatus.NORMAL);
            mPlayStartMsec = jAudioView.pixelsToMillisecs(0);
            //mPlayEndMsec = jAudioView.pixelsToMillisecsTotal();
            mPlayEndMsec = mediaPlayer.getDuration();

            mediaPlayer.start();
            Message msg = new Message();
            updateTime.sendMessage(msg);
            btnPlay.setText("暂停");
        }*/
    }

    /** 更新播放进度*/
    private void updateDisplay() {
        int now = mediaPlayer.getCurrentPosition();
        int frames = jAudioView.millisecsToPixels(now*2);
        jAudioView.setCurrentLocation(getPos(now));//通过这个更新当前播放的位置
        Log.d("jin", "now:"+now);
        Log.d("jin", "getPos(now):"+getPos(now));
        Log.d("jin", "mPlayEndMsec"+mPlayEndMsec);
        if (now >= mPlayEndMsec ) {
            jAudioView.setJAudioStatus(JAudioStatus.END);
            try {
                mediaPlayer.reset();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setDataSource(file.getAbsolutePath());
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            btnPlay.setText("播放");
            updateDisplay();
        }
        jAudioView.invalidate();
    }

    private int getPos(int p){
        return p*jAudioView.getWaveFormCount()/mPlayEndMsec;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        mediaPlayer = null;
    }



    Handler jWaveFormViewHandler = new Handler() {
        public void handleMessage(Message msg) {
            updateJWaveFormView();
            currentPos ++;
            if (currentPos <= jWaveFormView.getAllLocation()){
                jWaveFormViewHandler.sendMessageDelayed(new Message(), 100);
            }else{
                currentPos = 0;
                updateJWaveFormView();
            }

        }
    };

    private int currentPos = 0;

    private void updateJWaveFormView() {
        jWaveFormView.setCurrentLocation(currentPos);
        jWaveFormView.invalidate();
    }
}