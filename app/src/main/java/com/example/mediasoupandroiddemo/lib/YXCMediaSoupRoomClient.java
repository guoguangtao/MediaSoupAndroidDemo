package com.example.mediasoupandroiddemo.lib;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONObject;
import org.mediasoup.droid.Logger;
import org.mediasoup.droid.MediasoupClient;
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

//    private Protoo mProtoo;

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
                    Log.i(TAG, "Receiver request : " + request.getMethod());
                }

                @Override
                public void onNotification(@NonNull Message.Notification notification) {
                    Log.i(TAG, "Receiver notification : " + notification.getMethod());
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

        // getRouterRtpCapabilities
        try {
            String routerRtpCapabilities = mPeer.syncRequest("getRouterRtpCapabilities");
            Log.i(TAG, "getRouterRtpCapabilities() result : " + routerRtpCapabilities);
        } catch (Exception e) {
            Log.w(TAG, "join room failed : " + e);
        }
    }
}
