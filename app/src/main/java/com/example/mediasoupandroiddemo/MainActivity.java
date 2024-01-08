package com.example.mediasoupandroiddemo;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mediasoupandroiddemo.lib.YXCMediaSoupRoomClient;

import org.webrtc.SurfaceViewRenderer;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

         SurfaceViewRenderer remoteView = findViewById(R.id.remote_view);
        new YXCMediaSoupRoomClient(getApplicationContext(), "12345", "ggt123456", remoteView);
    }
}