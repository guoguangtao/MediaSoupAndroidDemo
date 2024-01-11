package com.example.mediasoupandroiddemo;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mediasoupandroiddemo.yxclib.YXCMediaSoupRoomClient;

import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "YXCMainActivity";
    private SurfaceViewRenderer mRemoteView;

    private EglBase mEglBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate : " + this);

//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setContentView(R.layout.activity_main);

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(true);

        mRemoteView = findViewById(R.id.remote_view);
        mEglBase = EglBase.create();
        Log.i(TAG, "mEglBase.hasSurface : " + mEglBase.hasSurface());
        mRemoteView.init(mEglBase.getEglBaseContext(), new RendererCommon.RendererEvents() {
            @Override
            public void onFirstFrameRendered() {
                Log.i(TAG, "onFirstFrameRendered");
            }

            @Override
            public void onFrameResolutionChanged(int videoWidth, int videoHeight, int rotation) {
                Log.i(TAG, String.format("onFrameResolutionChanged : videoWith - %d, videoHeight " +
                        "- %d, rotation - %d", videoWidth, videoHeight, rotation));
            }
        });
        mRemoteView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);

        useMySelf();
    }

    private void useMySelf() {
        new YXCMediaSoupRoomClient(getApplicationContext(), "123456", "ggt123456", mRemoteView);
    }
}