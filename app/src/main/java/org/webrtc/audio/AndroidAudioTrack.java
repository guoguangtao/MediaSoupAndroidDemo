package org.webrtc.audio;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;

import org.webrtc.LeRTCAudioListener;

import org.webrtc.Logging;

/**
 * Author tcc
 * Date 2023/11/30 16:35
 * Description
 */
public class AndroidAudioTrack extends AudioTrack implements IAudioTrack {
    private final String TAG = "AndroidAudioTrack";

    public AndroidAudioTrack(AudioAttributes attributes, AudioFormat format, int bufferSizeInBytes, int mode, int sessionId) throws IllegalArgumentException {
        super(attributes, format, bufferSizeInBytes, mode, sessionId);
        Logging.d(TAG, "AndroidAudioTrack");
    }

    @Override
    public void setRTCListener(LeRTCAudioListener listener) {

    }
}
