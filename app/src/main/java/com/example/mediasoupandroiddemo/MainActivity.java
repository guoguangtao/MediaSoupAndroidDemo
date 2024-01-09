package com.example.mediasoupandroiddemo;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mediasoupandroiddemo.lib.RoomClient;
import com.example.mediasoupandroiddemo.lib.RoomOptions;
import com.example.mediasoupandroiddemo.lib.lv.RoomStore;
import com.example.mediasoupandroiddemo.yxclib.YXCMediaSoupRoomClient;

import org.mediasoup.droid.Logger;
import org.mediasoup.droid.MediasoupClient;
import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

public class MainActivity extends AppCompatActivity {

    private SurfaceViewRenderer mRemoteView;

    private EglBase mEGLBase;

    private RoomClient mRoomClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setContentView(R.layout.activity_main);

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(true);

        mRemoteView = findViewById(R.id.remote_view);
        mEGLBase = EglBase.create();
        mRemoteView.init(mEGLBase.getEglBaseContext(), null);
        mRemoteView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);

//        useMySelf();
        useThird();
    }

    private void useMySelf() {
        new YXCMediaSoupRoomClient(getApplicationContext(), "12345", "ggt123456", mRemoteView);
    }

    private void useThird() {
        Logger.setLogLevel(Logger.LogLevel.LOG_DEBUG);
        Logger.setDefaultHandler();
        MediasoupClient.initialize(getApplicationContext());
        RoomOptions roomOptions = new RoomOptions();
        RoomStore roomStore = new RoomStore();
       roomOptions.setProduce(false);
       roomOptions.setConsume(true);
       roomOptions.setForceTcp(true);
       roomOptions.setUseDataChannel(true);
       mRoomClient= new RoomClient(
                this, roomStore, "12345", "ggt123456", "guogt"
               , mRemoteView, false, false, roomOptions);
        mRoomClient.join();
    }
}