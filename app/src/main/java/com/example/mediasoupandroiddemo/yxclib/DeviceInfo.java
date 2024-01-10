package com.example.mediasoupandroiddemo.yxclib;

import android.os.Build;
import android.util.Log;

import org.json.JSONObject;

@SuppressWarnings("WeakerAccess")
public class DeviceInfo {

  private String mFlag;
  private String mName;
  private String mVersion;

  public String getFlag() {
    return mFlag;
  }

  public DeviceInfo setFlag(String flag) {
    this.mFlag = flag;
    return this;
  }

  public String getName() {
    return mName;
  }

  public DeviceInfo setName(String name) {
    this.mName = name;
    return this;
  }

  public String getVersion() {
    return mVersion;
  }

  public DeviceInfo setVersion(String version) {
    this.mVersion = version;
    return this;
  }

  public static DeviceInfo androidDevice() {
    return new DeviceInfo()
        .setFlag("android")
        .setName("Android " + Build.DEVICE)
        .setVersion(Build.VERSION.CODENAME);
  }

  public static DeviceInfo unknownDevice() {
    return new DeviceInfo().setFlag("unknown").setName("unknown").setVersion("unknown");
  }

  public JSONObject toJSONObject() {
    JSONObject deviceInfo = new JSONObject();
    try {
      deviceInfo.put("flag", getFlag());
      deviceInfo.put("name", getName());
      deviceInfo.put("version", getVersion());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return deviceInfo;
  }
}
