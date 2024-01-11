package com.example.mediasoupandroiddemo;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mediasoupandroiddemo.yxclib.YXCMediaSoupRoomClient;

import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

public class MainActivity extends AppCompatActivity {

    private SurfaceViewRenderer mRemoteView;

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
        EglBase mEGLBase = EglBase.create();
        mRemoteView.init(mEGLBase.getEglBaseContext(), null);
        mRemoteView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);

        useMySelf();
    }

    private void useMySelf() {
        new YXCMediaSoupRoomClient(getApplicationContext(), "123456", "ggt123456", mRemoteView);
    }
}