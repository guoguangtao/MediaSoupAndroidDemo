package com.example.mediasoupandroiddemo;

import static org.apache.http.conn.ssl.SSLSocketFactory.SSL;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;
import org.mediasoup.droid.Logger;
import org.protoojs.droid.Message;
import org.protoojs.droid.transports.AbsWebSocketTransport;

import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.ByteString;

public class WebSocketTransport extends AbsWebSocketTransport {

    private final String TAG = "YXC_WebSocketTransport";

    private final String mWebSocketUrl;

    private final OkHttpClient mClient;

    private WebSocket mWebSocket;

    public WebSocketTransport(String url) {
        super(url);
        mWebSocketUrl = url;
        mClient = getUnsafeOkHttpClient();
    }

    @Override
    public void connect(Listener listener) {

        if (TextUtils.isEmpty(mWebSocketUrl)) {
            Log.w(TAG, "WebSocket url is empty");
            return;
        }

        Log.i(TAG, "Will connect : " + mWebSocketUrl);

        Request request = new Request.Builder().url(mWebSocketUrl).addHeader("Sec-WebSocket-Protocol", "protoo").build();
        mClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                super.onClosed(webSocket, code, reason);

                Log.i(TAG, "WebSocket closed : " + code + reason);

                if (listener != null) {
                    listener.onClose();
                }
            }

            @Override
            public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                super.onClosing(webSocket, code, reason);

                Log.i(TAG, "WebSocket closing : " + code + " " + reason);
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
                super.onFailure(webSocket, t, response);

                Log.i(TAG, "WebSocket failed : " + response + " " + t.toString());

                if (listener != null) {
                    listener.onFail();
                }
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                super.onMessage(webSocket, text);

                Log.w(TAG, "WebSocket receiver text message : " + text);

                Message message = Message.parse(text);
                if (message == null) {
                    return;
                }
                if (listener != null) {
                    listener.onMessage(message);
                }
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
                super.onMessage(webSocket, bytes);

                Log.i(TAG, "WebSocket receiver bytes message : " + bytes);
            }

            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                super.onOpen(webSocket, response);

                Log.i(TAG, "WebSocket did open : " + response);

                mWebSocket = webSocket;

                if (listener != null) {
                    listener.onOpen();
                }
            }
        });
    }

    @Override
    public String sendMessage(JSONObject message) {

        String payload = message.toString();

        if (TextUtils.isEmpty(payload)) {
            Log.e(TAG, "send message is empty");
            return null;
        }

        if (mWebSocket == null) {
            Log.e(TAG, "send message failed websocket not connected");
            return null;
        }

        mWebSocket.send(payload);

        return payload;
    }

    @Override
    public void close() {

    }

    @Override
    public boolean isClosed() {
        return false;
    }

    private OkHttpClient getUnsafeOkHttpClient() {
        try {
            final TrustManager[] trustAllCerts =
                    new TrustManager[] {
                            new X509TrustManager() {

                                @Override
                                public void checkClientTrusted(
                                        java.security.cert.X509Certificate[] chain, String authType)
                                        throws CertificateException {}

                                @Override
                                public void checkServerTrusted(
                                        java.security.cert.X509Certificate[] chain, String authType)
                                        throws CertificateException {}

                                // Called reflectively by X509TrustManagerExtensions.
                                public void checkServerTrusted(
                                        java.security.cert.X509Certificate[] chain, String authType, String host) {}

                                @Override
                                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                    return new java.security.cert.X509Certificate[] {};
                                }
                            }
                    };

            final SSLContext sslContext = SSLContext.getInstance(SSL);
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            HttpLoggingInterceptor httpLoggingInterceptor =
                    new HttpLoggingInterceptor(s -> Logger.d(TAG, s));
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);

            OkHttpClient.Builder builder =
                    new OkHttpClient.Builder()
                            .addInterceptor(httpLoggingInterceptor)
                            .retryOnConnectionFailure(true);
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);

            builder.hostnameVerifier((hostname, session) -> true);

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
