package org.webrtc.audio;

import android.media.AudioFormat;
import android.media.AudioTrack;
import org.webrtc.LeRTCAudioListener;

import org.webrtc.AudioFrame;
import org.webrtc.Logging;

import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Author tcc
 * Date 2023/11/30 16:34
 * Description
 */
public class VirtualAudioTrack implements IAudioTrack {
    private final String TAG = "VirtualAudioTrack";
    private LeRTCAudioListener mRTCListener;
    private int mState = AudioTrack.STATE_UNINITIALIZED;
    private int mPlayState = AudioTrack.PLAYSTATE_STOPPED;
    private Thread mRenderThread;

    private LinkedBlockingQueue<AudioFrame> mAudioFrameQueue = new LinkedBlockingQueue<>();

    public VirtualAudioTrack(AudioFormat format, int bufferSizeInBytes, int mode, int sessionId) {
        mState = AudioTrack.STATE_INITIALIZED;
        Logging.d(TAG, "VirtualAudioTrack");
    }

    @Override
    public void setRTCListener(LeRTCAudioListener listener) {
        mRTCListener = listener;
    }

    @Override
    public void play() {
        mPlayState = AudioTrack.PLAYSTATE_PLAYING;
        if (mRenderThread != null && mRenderThread.isAlive()) {
            Logging.w(TAG, "play ignore");
            return;
        }
        mRenderThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int count = -1;
                while (mPlayState == AudioTrack.PLAYSTATE_PLAYING) {
                    try {
                        AudioFrame audioFrame = mAudioFrameQueue.take();
//                        if (count++ % 1000 == 0) {
//                            Logging.w(TAG, "play");
//                        }
                        if (mRTCListener != null) {
                            mRTCListener.onAudioFrame(audioFrame);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mRenderThread.start();
    }

    @Override
    public void stop() {
        mPlayState = AudioTrack.PLAYSTATE_STOPPED;
    }

    @Override
    public int getPlayState() {
        return mPlayState;
    }

    int aCount = -1;

    @Override
    public int write(ByteBuffer byteBuffer, int offsetInBytes, int sizeInBytes) {
        if (mRTCListener != null && mPlayState == AudioTrack.PLAYSTATE_PLAYING) {
            AudioFrame audioFrame = new AudioFrame();
            byte[] audioData = new byte[byteBuffer.limit() - byteBuffer.position()];
            byteBuffer.get(audioData);
            audioFrame.originData = audioData;
            mAudioFrameQueue.offer(audioFrame);
            try {
                Thread.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return offsetInBytes;
    }

    @Override
    public int getUnderrunCount() {
        return -2;
    }

    @Override
    public int getBufferSizeInFrames() {
        return -1;
    }

    @Override
    public int getPlaybackRate() {
        return 0;
    }

    @Override
    public int setBufferSizeInFrames(int bufferSizeInFrames) {
        return 0;
    }

    @Override
    public int getBufferCapacityInFrames() {
        return 0;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override
    public int getChannelCount() {
        return 0;
    }

    @Override
    public int getSampleRate() {
        return 0;
    }

    @Override
    public int getState() {
        return mState;
    }

    @Override
    public void release() {
        mState = AudioTrack.STATE_UNINITIALIZED;
        mPlayState = AudioTrack.PLAYSTATE_STOPPED;
    }
}
