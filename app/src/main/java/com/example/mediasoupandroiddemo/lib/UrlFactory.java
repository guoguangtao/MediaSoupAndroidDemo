package com.example.mediasoupandroiddemo.lib;

import android.util.Log;

import java.util.Locale;

public class UrlFactory {
  private static final String TAG = "YXCUrlFactory";

  private static final String HOSTNAME = "43.139.208.247";
  //  private static final String HOSTNAME = "192.168.1.103";
  private static final int PORT = 4443;

  public static String getInvitationLink(String roomId, boolean forceH264, boolean forceVP9) {
    String url = String.format(Locale.US, "https://%s:3000/?roomId=%s", HOSTNAME, roomId);
    if (forceH264) {
      url += "&forceH264=true";
    } else if (forceVP9) {
      url += "&forceVP9=true";
    }
    Log.i(TAG, "邀请链接 : " + url);
    return url;
  }

  public static String getProtooUrl(
          String roomId, String peerId, boolean forceH264, boolean forceVP9) {
    String url =
            String.format(
                    Locale.US, "wss://%s:%d/?roomId=%s&peerId=%s", HOSTNAME, PORT, roomId, peerId);
    if (forceH264) {
      url += "&forceH264=true";
    } else if (forceVP9) {
      url += "&forceVP9=true";
    }
    Log.i(TAG, "WebSocket 连接地址 : " + url);
    return url;
  }
}
