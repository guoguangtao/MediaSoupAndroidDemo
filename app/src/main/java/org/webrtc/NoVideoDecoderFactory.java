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

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class that combines HW and SW decoders.
 */
public class NoVideoDecoderFactory implements VideoDecoderFactory {
    private final String TAG = "NoVideoDecoderFactory";
    private AndroidVideoNoDecoder mAndroidVideoNoDecoder;
    private VideoSink mVideoSink;

    /**
     * Create decoder factory using default hardware decoder factory.
     */
    public NoVideoDecoderFactory(@Nullable EglBase.Context eglContext) {
    }

    /**
     * Create decoder factory using explicit hardware decoder factory.
     */
    NoVideoDecoderFactory(VideoDecoderFactory hardwareVideoDecoderFactory) {
    }

    @Override
    public @Nullable VideoDecoder createDecoder(VideoCodecInfo codecType) {
        mAndroidVideoNoDecoder = new AndroidVideoNoDecoder();
        mAndroidVideoNoDecoder.setVideoSink(mVideoSink);
        return mAndroidVideoNoDecoder;
    }

    public void setVideoSink(VideoSink videoSink) {
        Logging.w(TAG, "setVideoSink " + videoSink);
        if (mAndroidVideoNoDecoder != null) {
            mAndroidVideoNoDecoder.setVideoSink(videoSink);
        }
        mVideoSink = videoSink;
    }

    //    @Override
    public VideoCodecInfo[] getSupportedCodecs() {
        List<VideoCodecInfo> supportedCodecInfos = new ArrayList<VideoCodecInfo>();
        // Generate a list of supported codecs in order of preference:
        // VP8, VP9, H264 (high profile), H264 (baseline profile), AV1 and H265.
        for (VideoCodecMimeType type :
                new VideoCodecMimeType[]{VideoCodecMimeType.H264}) {
            MediaCodecInfo codec = findCodecForType(type);
            if (codec != null) {
                String name = type.name();
                supportedCodecInfos.add(new VideoCodecInfo(
                        name, MediaCodecUtils.getCodecProperties(type, /* highProfile= */ false)));
            }
        }
        return supportedCodecInfos.toArray(new VideoCodecInfo[supportedCodecInfos.size()]);
    }

    private @Nullable MediaCodecInfo findCodecForType(VideoCodecMimeType type) {
        for (int i = 0; i < MediaCodecList.getCodecCount(); ++i) {
            MediaCodecInfo info = null;
            try {
                info = MediaCodecList.getCodecInfoAt(i);
            } catch (IllegalArgumentException e) {
                Logging.e(TAG, "Cannot retrieve decoder codec info", e);
            }

            if (info == null || info.isEncoder()) {
                continue;
            }

            if (isSupportedCodec(info, type)) {
                return info;
            }
        }

        return null; // No support for this type.
    }

    private boolean isSupportedCodec(MediaCodecInfo info, VideoCodecMimeType type) {
        if (!MediaCodecUtils.codecSupportsType(info, type)) {
            return false;
        }
        // Check for a supported color format.
        if (MediaCodecUtils.selectColorFormat(
                MediaCodecUtils.DECODER_COLOR_FORMATS, info.getCapabilitiesForType(type.mimeType()))
                == null) {
            return false;
        }
        return true;
    }
}
