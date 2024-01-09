/*
 *  Copyright 2017 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package org.webrtc;

/**
 * Android hardware video decoder.
 */
class AndroidVideoNoDecoder implements VideoDecoder, VideoSink {
    private static final String TAG = "AndroidVideoNoDecoder";
    private Callback mCallback;
    private VideoSink mVideoSink;

    AndroidVideoNoDecoder() {
        Logging.d(TAG, "AndroidVideoNoDecoder");
    }

    @Override
    public VideoCodecStatus initDecode(Settings settings, Callback callback) {
        Logging.w(TAG, "initDecode " + callback);
        mCallback = callback;
        return VideoCodecStatus.OK;
    }

    public void setVideoSink(VideoSink videoSink) {
        Logging.w(TAG, "setVideoSink " + videoSink);
        mVideoSink = videoSink;
    }

    @Override
    public VideoCodecStatus release() {
        return VideoCodecStatus.OK;
    }

    @Override
    public VideoCodecStatus decode(EncodedImage frame, DecodeInfo info) {
        Logging.w(TAG, "decode " + frame.encodedWidth + " / " + frame.encodedHeight);
        if (mCallback != null) {
            mCallback.onDecodedFrame(new VideoFrame(frame.buffer, 0, frame.captureTimeMs), null, null);
        }
        if (mVideoSink != null) {
            mVideoSink.onFrame(new VideoFrame(frame.buffer, 0, frame.captureTimeMs));
        }
        return VideoCodecStatus.OK;
    }

    @Override
    public String getImplementationName() {
        return "";
    }

    @Override
    public void onFrame(VideoFrame frame) {
        Logging.w(TAG, "onFrame " + frame.getBuffer().getWidth() + " / " + frame.getBuffer().getHeight());
    }
}
