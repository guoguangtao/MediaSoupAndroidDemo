package com.example.mediasoupandroiddemo;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;
import org.mediasoup.droid.Device;
import org.mediasoup.droid.MediasoupClient;
import org.mediasoup.droid.RecvTransport;
import org.mediasoup.droid.Transport;
import org.protoojs.droid.Message;
import org.protoojs.droid.transports.AbsWebSocketTransport;

public class MainActivity extends AppCompatActivity {

    private final long GETROUTERRTPCAPABILITIESREQUESTID = 1000;

    private final long CREATEWEBRTCTRANSPORTREQUESTID = 10001;

    private final long JOINROOMREQUESTID = 10002;

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
            // 修改 requestId
            request.put("id", GETROUTERRTPCAPABILITIESREQUESTID);
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
            String jsonString = response.getData().toString();
            long requestId = response.getId();
            if (requestId == GETROUTERRTPCAPABILITIESREQUESTID) {
                // getRouterRtpCapabilities
                setupDevice(jsonString);
                sendCreateReceiverTransport();
                joinRoom();
            } else if (requestId == CREATEWEBRTCTRANSPORTREQUESTID) {
                // createWebRtcTransport
                createReceiverTransport(jsonString);
            }
        } else {
            // 请求失败
            Log.i(TAG, "errorCode : " + response.getErrorCode() + " errorReason : " + response.getErrorReason());
        }
    }

    private void handleNotification(Message.Notification notification) {

    }

    private void setupDevice(String routerRtpCapabilities) {
        try {
            mMediaSoupDevice = new Device();
            mMediaSoupDevice.load(routerRtpCapabilities, null);
        } catch (Exception e) {
            Log.w(TAG, "Setup device exception : " + e);
        }
    }

    private void sendCreateReceiverTransport() {
        try {
            JSONObject request = Message.createRequest("createWebRtcTransport", new JSONObject());
            request.put("id", CREATEWEBRTCTRANSPORTREQUESTID);
            request.put("forceTcp", false);
            request.put("producing", false);
            request.put("consuming", true);
            Log.i(TAG, "sendCreateReceiverTransport : " + request);
            mSocketTransport.sendMessage(request);
        } catch (Exception e) {
            Log.e(TAG, "sendCreateReceiverTransport exception : " + e);
        }

    }

    private void createReceiverTransport(String jsonString) {
        try {
            JSONObject info = new JSONObject(jsonString);
            String id = info.optString("id");
            String iceParameters = info.optString("iceParameters");
            String iceCandidates = info.optString("iceCandidates");
            String dtlsParameters = info.optString("dtlsParameters");
            String sctpParameters = info.optString("sctpParameters");
            if (TextUtils.isEmpty(sctpParameters)) {
                sctpParameters = null;
            }
            Log.i(TAG, "createReceiverTransport");
            mMediaSoupDevice.createRecvTransport(new RecvTransport.Listener() {
                @Override
                public void onConnect(Transport transport, String dtlsParameters) {
                    Log.i(TAG, "createReceiverTransport onConnect : " + dtlsParameters);
                }

                @Override
                public void onConnectionStateChange(Transport transport, String connectionState) {
                    Log.i(TAG, "createReceiverTransport onConnectionStateChange : " + connectionState);
                }
            }, id, iceParameters, iceCandidates, dtlsParameters, sctpParameters);
        } catch (Exception e) {
            Log.e(TAG, "createReceiverTransport exception : " + e);
        }
    }

    private void joinRoom() {
        try {
            JSONObject request = Message.createRequest("join", new JSONObject());
            request.put("id", JOINROOMREQUESTID);
            request.put("displayName", "ggt");
            String rtpCapabilities = mMediaSoupDevice.getRtpCapabilities();
            if (!TextUtils.isEmpty(rtpCapabilities)) {
                request.put("rtpCapabilities", new JSONObject(rtpCapabilities));
            }
            mSocketTransport.sendMessage(request);
        } catch (Exception e) {
            Log.e(TAG, "join room exception : " + e);
        }

    }
}