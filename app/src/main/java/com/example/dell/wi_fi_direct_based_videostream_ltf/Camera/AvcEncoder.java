package com.example.dell.wi_fi_direct_based_videostream_ltf.Camera;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class AvcEncoder {

    private final static String TAG=AvcEncoder.class.getSimpleName();
    private MediaCodec mediaCodec;
    private int m_width;
    private int m_height;
    private byte[] m_info = null;

    private int mColorFormat;
    private MediaCodecInfo codecInfo;
    private static final String MIME_TYPE = "video/avc"; // H.264 Advanced Video
    private byte[] yuv420 = null;
    AvcEncoder(int width, int height, int framerate, int bitrate) throws IOException {

        m_width  = width;
        m_height = height;
        Log.v("xmc", "AvcEncoder:"+m_width+"+"+m_height);
        yuv420 = new byte[width*height*3/2];

        mediaCodec = MediaCodec.createEncoderByType(MIME_TYPE);
        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, framerate);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);//关键帧间隔时间 单位s

        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mediaCodec.start();
    }


    public void close() {
        try {
            mediaCodec.stop();
            mediaCodec.release();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public  void offerEncoder(byte[] input, byte[] output) {
        Log.v("xmc", "offerEncoder:"+input.length+"+"+output.length);
        int pos = 0;
//        swapYV12toI420(input, yuv420, m_width, m_height);
//        NV21toI420SemiPlanar(input, yuv420, m_width, m_height);
        try {
            /**
             * 以下是缓冲区数组
             */
            ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
            ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
            /**
             * dequeueInputBuffer()函数解释
             * Returns the index of an input buffer to be filled with valid data or -1 if no such buffer is currently available.
             * This method will return immediately if timeoutUs == 0, wait indefinitely for the availability of an input buffer if timeoutUs < 0
             * or wait up to "timeoutUs" microseconds if timeoutUs > 0.
             */
            int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
            if (inputBufferIndex >= 0) {
                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                inputBuffer.put(input);
                mediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length, 0, 0);

            }

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo,0);
            Log.d(TAG, "offerEncoder: 111111:"+outputBufferIndex+"buffinfosize:"+bufferInfo.size);

            while (outputBufferIndex >= 0) {
                ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                byte[] outData = new byte[bufferInfo.size];
                outputBuffer.get(outData);


                if(m_info != null){
                    System.arraycopy(outData, 0,  output, pos, outData.length);
                    pos += outData.length;
                }else{//保存pps sps 只有开始时 第一个帧里有， 保存起来后面用
                    ByteBuffer spsPpsBuffer = ByteBuffer.wrap(outData);
                    Log.v("xmc", "swapYV12toI420:outData:"+outData);
                    Log.v("xmc", "swapYV12toI420:spsPpsBuffer:"+spsPpsBuffer);
                    for(int i=0;i<outData.length;i++){
                        //输出SPS和PPS循环
                        Log.e("xmc333", "run: get data rtpData[i]="+i+":"+outData[i]);
                    }

                    if (spsPpsBuffer.getInt() == 0x00000001) {
                        m_info = new byte[outData.length];
                        System.arraycopy(outData, 0, m_info, 0, outData.length);
                    }else {
                        return ;
                    }
                }
                //key frame 编码器生成关键帧时只有 00 00 00 01 65 没有pps sps， 要加上
                if(output[4] == 0x65) {
                    System.arraycopy(m_info, 0,  output, 0, m_info.length);
                    System.arraycopy(outData, 0,  output, m_info.length, outData.length);
                }
                mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            }


        } catch (Throwable t) {
            t.printStackTrace();
        }
        Log.v("xmc", "offerEncoder+pos:"+pos);
//        return pos;
    }

    //网友提供的，如果swapYV12toI420方法颜色不对可以试下这个方法，不同机型有不同的转码方式
    private void NV21toI420SemiPlanar(byte[] nv21bytes, byte[] i420bytes, int width, int height) {
        Log.v("xmc", "NV21toI420SemiPlanar:::"+width+"+"+height);
        final int iSize = width * height;
        System.arraycopy(nv21bytes, 0, i420bytes, 0, iSize);

        for (int iIndex = 0; iIndex < iSize / 2; iIndex += 2) {
            i420bytes[iSize + iIndex / 2 + iSize / 4] = nv21bytes[iSize + iIndex]; // U
            i420bytes[iSize + iIndex / 2] = nv21bytes[iSize + iIndex + 1]; // V
        }
    }

    //yv12 转 yuv420p  yvu -> yuv
    private void swapYV12toI420(byte[] yv12bytes, byte[] i420bytes, int width, int height) {
        Log.v("xmc", "swapYV12toI420:::"+width+"+"+height);
        Log.v("xmc", "swapYV12toI420:::"+yv12bytes.length+"+"+i420bytes.length+"+"+width * height);
        System.arraycopy(yv12bytes, 0, i420bytes, 0, width*height);
        System.arraycopy(yv12bytes, width*height+width*height/4, i420bytes,       width*height,width*height/4);
        System.arraycopy(yv12bytes, width*height, i420bytes, width*height+width*height/4,width*height/4);
    }
    //public static void arraycopy(Object src,int srcPos,Object dest,int destPos,int length)
    //src:源数组；  srcPos:源数组要复制的起始位置；
    //dest:目的数组；    destPos:目的数组放置的起始位置；    length:复制的长度。
}
