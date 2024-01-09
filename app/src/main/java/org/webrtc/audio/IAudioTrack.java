package org.webrtc.audio;

import org.webrtc.LeRTCAudioListener;

import java.nio.ByteBuffer;

/**
 * Author tcc
 * Date 2023/11/30 16:34
 * Description
 */
public interface IAudioTrack {

    void play();

    void stop();

    int getPlayState();

    int write(ByteBuffer audioData, int offsetInBytes, int sizeInBytes);

    int getUnderrunCount();

    int getBufferSizeInFrames();

    int getPlaybackRate();

    int setBufferSizeInFrames(int bufferSizeInFrames);

    int getBufferCapacityInFrames();

    int getAudioSessionId();

    int getChannelCount();

    int getSampleRate();

    int getState();

    void release();

    void setRTCListener(LeRTCAudioListener listener);
}
