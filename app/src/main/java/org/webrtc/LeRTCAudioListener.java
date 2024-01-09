package org.webrtc;

/**
 * 音频数据回调
 */
public interface LeRTCAudioListener {

    default void onAudioFrame(AudioFrame audioFrame) {
    }
}
