package com.example.dell.wi_fi_direct_based_videostream_ltf.Coder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

import java.nio.ByteBuffer;

import static com.example.dell.wi_fi_direct_based_videostream_ltf.Camera.CameraActivity.TAG;

public class MyDecoder {
    private MediaCodec codec;
    private final String MIME_TYPE = "video/avc";

    void init(Surface surface){
        try {
            codec = MediaCodec.createEncoderByType(MIME_TYPE);
        }catch (Exception e){
            e.printStackTrace();
            codec.configure(MediaFormat.createVideoFormat(MIME_TYPE,16,32),surface,null,0);
            codec.start();
        }
    }
    public void startDecode(){
        MediaCodec.BufferInfo info=new MediaCodec.BufferInfo();
        long startMs = System.currentTimeMillis();
        int inIndex=codec.dequeueInputBuffer(1000);
        if (inIndex>=0){
        ByteBuffer byteBuffer=codec.getInputBuffer(inIndex);
            Log.d(TAG, "startDecode: >>buffer"+byteBuffer);
            assert byteBuffer != null;
            byteBuffer.clear();

        }

    }



}
