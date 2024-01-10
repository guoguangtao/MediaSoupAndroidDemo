package com.example.mediasoupandroiddemo.yxclib;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mediasoupandroiddemo.lib.Protoo;

import org.json.JSONObject;
import org.protoojs.droid.Peer;
import org.protoojs.droid.ProtooException;
import org.protoojs.droid.transports.AbsWebSocketTransport;

import io.reactivex.Observable;

public class YXCPeer extends Peer {

    private final String TAG = "YXCPeer";

    public YXCPeer(@NonNull AbsWebSocketTransport transport, @NonNull Listener listener) {
        super(transport, listener);
    }

    public String syncRequest(String method) throws ProtooException {
        return syncRequest(method, new JSONObject());
    }

    public String syncRequest(String method, @Nullable JSONObject data) throws ProtooException {
        Log.d(TAG, "syncRequest(), method: " + method);

        try {
            if (data == null) {
                data = new JSONObject();
            }
            return request(method, data).blockingFirst();
        } catch (Throwable throwable) {
            throw new ProtooException(-1, throwable.getMessage());
        }
    }

    public Observable<String> request(String method, @NonNull JSONObject data) {
        Log.d(TAG, "request(), method: " + method);
        return Observable.create(
                emitter ->
                        request(
                                method,
                                data,
                                new ClientRequestHandler() {
                                    @Override
                                    public void resolve(String data) {
                                        if (!emitter.isDisposed()) {
                                            emitter.onNext(data);
                                        }
                                    }

                                    @Override
                                    public void reject(long error, String errorReason) {
                                        if (!emitter.isDisposed()) {
                                            emitter.onError(new ProtooException(error, errorReason));
                                        }
                                    }
                                }));
    }
}
