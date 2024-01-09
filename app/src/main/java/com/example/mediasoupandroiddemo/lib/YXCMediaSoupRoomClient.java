package com.example.mediasoupandroiddemo.lib;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONObject;
import org.mediasoup.droid.Device;
import org.mediasoup.droid.Logger;
import org.mediasoup.droid.MediasoupClient;
import org.mediasoup.droid.RecvTransport;
import org.mediasoup.droid.Transport;
import org.protoojs.droid.Message;
import org.protoojs.droid.Peer;
import org.protoojs.droid.ProtooException;
import org.protoojs.droid.transports.AbsWebSocketTransport;
import org.webrtc.SurfaceViewRenderer;

import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

public class YXCMediaSoupRoomClient {

    private final String TAG = "YXCMediaSoupRoomClient";
    /** 房间 ID */
    private final String mRoomId;
    /** 用户 ID */
    private final String mUserId;
    /** WebRtc 渲染 View */
    private final SurfaceViewRenderer mRemoteView;
    /** wss://43.139.208.247:4443/?roomId=12345&peerId=ggt123 */
    private final String mWebSocketUrl = "wss://43.139.208.247:4443";

    private final Handler mWorkHandler;

    private final Handler mMainHandler;

    private YXCWebSocketTransport mWebSocketTransport;

    private YXCPeer mPeer;

    private Device mMediaSoupDevice;

    private Transport mReceiverTransport;

    public YXCMediaSoupRoomClient(Context context, String roomId, String userId
            , SurfaceViewRenderer remoteView) {
        mRoomId = roomId;
        mUserId = userId;
        mRemoteView = remoteView;
        // 创建一个新的非主线程
        HandlerThread handlerThread = new HandlerThread("worker");
        handlerThread.start();
        mWorkHandler = new Handler(handlerThread.getLooper());
        // 获取到主线程
        mMainHandler = new Handler(Looper.getMainLooper());
        // 初始化 MediaSoup
        MediasoupClient.initialize(context);
        // 连接 WebSocket
        connectWebSocket();
    }

    /**
     * 连接 WebSocket
     */
    private void connectWebSocket() {
        String url = String.format(Locale.US, "%s/?roomId=%s&peerId=%s", mWebSocketUrl
                , mRoomId, mUserId);
        Log.i(TAG, "连接 WebSocket : " + url);
        mWorkHandler.post(() -> {
            mWebSocketTransport = new YXCWebSocketTransport(url);

            mPeer = new YXCPeer(mWebSocketTransport, new Peer.Listener() {
                @Override
                public void onOpen() {
                    Log.i(TAG, "WebSocket did open");
                    mWorkHandler.post(() -> joinRoom());
                }

                @Override
                public void onFail() {
                    Log.i(TAG, "WebSocket failed");
                }

                @Override
                public void onRequest(@NonNull Message.Request request, @NonNull Peer.ServerRequestHandler handler) {
                    Log.i(TAG, "Receiver request : " + request.getMethod() + " data : " + request.getData());
                }

                @Override
                public void onNotification(@NonNull Message.Notification notification) {
                    Log.i(TAG, "Receiver notification : " + notification.getMethod() + " data : " + notification.getData());
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
        });
    }

    /**
     * 加入房间
     */
    private void joinRoom() {
        Log.i(TAG, "Join Room : " + mRoomId);

        try {
            // getRouterRtpCapabilities
            String routerRtpCapabilities = mPeer.syncRequest("getRouterRtpCapabilities");
            // 创建 Device
            mMediaSoupDevice = new Device();
            mMediaSoupDevice.load(routerRtpCapabilities, null);
            // 创建消费者(拉流)
            createReceiverTransport();
            // 加入房间
            JSONObject joinRoomParameters = new JSONObject();
            joinRoomParameters.put("displayName" , "guogt");
            String rtpCapabilities = mMediaSoupDevice.getRtpCapabilities();
            joinRoomParameters.put("rtpCapabilities", new JSONObject(rtpCapabilities));
            String sctpCapabilities = mMediaSoupDevice.getSctpCapabilities();
            joinRoomParameters.put("sctpCapabilities", sctpCapabilities);
            String result = mPeer.syncRequest("join", joinRoomParameters);
            Log.i(TAG, "Join room result : " + result);
        } catch (Exception e) {
            Log.w(TAG, "join room failed : " + e);
        }
    }

    /**
     * 创建拉流 transport
     */
    private void createReceiverTransport() {
        Log.i(TAG, "createReceiverTransport");

        try {
            String method = "createWebRtcTransport";
            JSONObject parameter = new JSONObject();
            parameter.put("forceTcp", true);
            parameter.put("producing" , false);
            parameter.put("consuming", true);
            parameter.put("sctpCapabilities", mMediaSoupDevice.getSctpCapabilities());
            String result = mPeer.syncRequest(method, parameter);
            Log.i(TAG, "createWebRtcTransport result : " + result);
            JSONObject info = new JSONObject(result);
            String id = info.optString("id");
            String iceParameters = info.optString("iceParameters");
            String iceCandidates = info.optString("iceCandidates");
            String dtlsParameters = info.optString("dtlsParameters");
            String sctpParameters = info.optString("sctpParameters");
            mReceiverTransport = mMediaSoupDevice.createRecvTransport(new RecvTransport.Listener() {
                @Override
                public void onConnect(Transport transport, String dtlsParameters) {
                    Log.i(TAG, "createRecvTransport onConnect : " + dtlsParameters);
                }

                @Override
                public void onConnectionStateChange(Transport transport, String connectionState) {
                    Log.i(TAG, "createRecvTransport onConnectionStateChange : " + connectionState);
                }
            }, id, iceParameters, iceCandidates, dtlsParameters, sctpParameters);

        } catch (Exception e) {
            Log.w(TAG, "createReceiverTransport exception : " + e);
        }
    }
}
