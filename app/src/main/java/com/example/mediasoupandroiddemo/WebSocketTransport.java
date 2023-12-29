package com.example.mediasoupandroiddemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mediasoupandroiddemo.protoo.transports.AbsWebSocketTransport;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketTransport extends AbsWebSocketTransport {

    private final String mWebSocketUrl = "";

    private OkHttpClient mClient;

    public WebSocketTransport(String url) {
        super(url);
    }

    @Override
    public void connect(Listener listener) {
        mClient = new OkHttpClient.Builder().pingInterval(10, TimeUnit.SECONDS).build();
        Request request = new Request.Builder().url(mWebSocketUrl).build();
        mClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                super.onClosed(webSocket, code, reason);
            }

            @Override
            public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                super.onClosing(webSocket, code, reason);
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
                super.onFailure(webSocket, t, response);
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                super.onMessage(webSocket, text);
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
                super.onMessage(webSocket, bytes);
            }

            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                super.onOpen(webSocket, response);
            }
        });
    }

    @Override
    public String sendMessage(JSONObject message) {
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    public boolean isClosed() {
        return false;
    }
}
