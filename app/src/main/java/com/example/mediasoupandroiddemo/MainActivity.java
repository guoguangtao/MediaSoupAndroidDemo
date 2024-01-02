package com.example.mediasoupandroiddemo;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;
import org.mediasoup.droid.Device;
import org.mediasoup.droid.MediasoupClient;
import org.protoojs.droid.Message;
import org.protoojs.droid.transports.AbsWebSocketTransport;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "YXC_MainActivity";

    private WebSocketTransport mSocketTransport;

    private Device mMediaSoupDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MediasoupClient.initialize(getApplicationContext());
        String websocketUrl = "wss://43.139.208.247:4443/?roomId=j0hfsm06&peerId=ggt123&forceH264=true";
        mSocketTransport = new WebSocketTransport(websocketUrl);
        mSocketTransport.connect(new AbsWebSocketTransport.Listener() {
            @Override
            public void onOpen() {

                Log.i(TAG, "WebSocket did open");
                joinImpl();
            }

            @Override
            public void onFail() {

                Log.i(TAG, "WebSocket open failed");
            }

            @Override
            public void onMessage(Message message) {

                Log.i(TAG, "WebSocket receiver message : " + message.toString());
                if (message instanceof Message.Response) {
                    handleResponse((Message.Response) message);
                } else if (message instanceof Message.Request) {
                    handleRequest((Message.Request) message);
                } else if (message instanceof Message.Notification) {
                    handleNotification((Message.Notification) message);
                }
            }

            @Override
            public void onDisconnected() {

                Log.i(TAG, "WebSocket disconnected");
            }

            @Override
            public void onClose() {

                Log.i(TAG, "WebSocket closed");
            }
        });
    }

    private void joinImpl() {
        Log.i(TAG, "Join room completion");

        try {
            JSONObject request = Message.createRequest("getRouterRtpCapabilities", new JSONObject());
            Log.i(TAG, "Join room : " + request);
            mSocketTransport.sendMessage(request);
        } catch (Exception e) {
            Log.w(TAG, e);
        }
    }

    private void handleRequest(Message.Request request) {}

    private void handleResponse(Message.Response response) {
        if (response.isOK()) {
            // 请求成功
            String routerRtpCapabilities = response.getData().toString();
            setupDevice(routerRtpCapabilities);
        } else {
            // 请求失败
            Log.i(TAG, "errorCode : " + response.getErrorCode() + " errorReason : " + response.getErrorReason());
        }
    }

    private void handleNotification(Message.Notification notification) {

    }

    private void setupDevice(String routerRtpCapabilities) {
        try {
            Log.i(TAG, "routerRtpCapabilities : " + routerRtpCapabilities);
            mMediaSoupDevice = new Device();
            mMediaSoupDevice.load(routerRtpCapabilities, null);
            Log.i(TAG, "Rtp : " + mMediaSoupDevice.getRtpCapabilities());
        } catch (Exception e) {
            Log.w(TAG, "Setup device exception : " + e);
        }
    }
}