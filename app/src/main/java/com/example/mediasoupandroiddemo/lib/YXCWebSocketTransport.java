package com.example.mediasoupandroiddemo.lib;

import static org.apache.http.conn.ssl.SSLSocketFactory.SSL;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;
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

public class YXCWebSocketTransport extends AbsWebSocketTransport {

    private final String TAG = "YXCWebSocketTransport";

    private final OkHttpClient mOkHttpClient;

    private final Handler mHandler;

    private Listener mListener;

    private boolean mClosed;

    private WebSocket mWebSocket;

    public YXCWebSocketTransport(String url) {
        super(url);

        mOkHttpClient = getUnsafeOkHttpClient();

        HandlerThread handlerThread = new HandlerThread("socket");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
    }

    @Override
    public void connect(Listener listener) {
        mListener = listener;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                connectWebSocket();
            }
        });
    }

    @Override
    public String sendMessage(JSONObject message) {

        if (message == null) {
            return "message is null";
        }

        String messageString = message.toString();

        mHandler.post(() -> {
            if (mClosed) {
                return;
            }

            if (mWebSocket != null) {
                Log.i(TAG, "send message : " + messageString);
                mWebSocket.send(messageString);
            }
        });

        return messageString;
    }

    @Override
    public void close() {
        if (mClosed) {
            return;
        }

        mClosed = true;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mWebSocket != null) {
                    mWebSocket.close(1000, "bye");
                    mWebSocket = null;
                }
            }
        });
    }

    @Override
    public boolean isClosed() {
        return mClosed;
    }

    private void connectWebSocket() {
        mOkHttpClient.newWebSocket(new Request.Builder().url(mUrl)
                        .addHeader("Sec-WebSocket-Protocol", "protoo").build()
                , new WebSocketListener() {

                    @Override
                    public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                        super.onOpen(webSocket, response);

                        Log.i(TAG, "on open");

                        if (mClosed) {
                            return;
                        }

                        mWebSocket = webSocket;

                        if (mListener != null) {
                            mListener.onOpen();
                        }
                    }

                    @Override
                    public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                        super.onClosed(webSocket, code, reason);

                        Log.i(TAG, "on closed " + reason);

                        if (mClosed) {
                            return;
                        }

                        mClosed = true;

                        if (mListener != null) {
                            mListener.onClose();
                        }
                    }

                    @Override

                    public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                        super.onClosing(webSocket, code, reason);
                        Log.i(TAG, "on closed " + reason);
                    }

                    @Override
                    public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
                        super.onFailure(webSocket, t, response);
                        Log.i(TAG, "on failed " + response);

                        if (mClosed) {
                            return;
                        }

                        if (mListener != null) {
                            mListener.onFail();
                        }
                    }

                    @Override
                    public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                        super.onMessage(webSocket, text);

                        if (mClosed) {
                            return;
                        }

                        Message message = Message.parse(text);

                        if (message == null) {
                            return;
                        }

                        if (mListener != null) {
                            mListener.onMessage(message);
                        }
                    }

                    @Override
                    public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
                        super.onMessage(webSocket, bytes);
                        Log.i(TAG, "on message bytes");
                    }
                });
    }

    /**
     * 使用 OkHttp 非安全方案进行连接
     */
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
                    new HttpLoggingInterceptor(s -> Log.i(TAG, "OkHttpLog : " + s));
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
