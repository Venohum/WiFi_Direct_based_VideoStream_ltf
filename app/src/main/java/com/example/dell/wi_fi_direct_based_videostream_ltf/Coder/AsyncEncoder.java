package com.example.dell.wi_fi_direct_based_videostream_ltf.Coder;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Surface;

import com.example.dell.wi_fi_direct_based_videostream_ltf.Camera.Util;
import com.example.dell.wi_fi_direct_based_videostream_ltf.UDP.EchoClient;
import com.example.dell.wi_fi_direct_based_videostream_ltf.Multicast.MulticastClient;
import com.example.dell.wi_fi_direct_based_videostream_ltf.wifi_direct.DeviceDetailFragment;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;

public class AsyncEncoder {
    private final static String TAG = "VideoEncoder";
    private final static int CONFIGURE_FLAG_ENCODE = MediaCodec.CONFIGURE_FLAG_ENCODE;
    private final static int CACHE_BUFFER_SIZE = 8;
    private byte[] mSps,mPps;
    private MediaCodec  mMediaCodec;
    public Surface mSurface;
    private MediaFormat mMediaFormat;
    private long number=0;
    byte[] temp=null;
    private int      mViewWidth;
    private int      mViewHeight;
    private EchoClient echoClient=new EchoClient("192.168.49.125");
    private EchoClient echoClient2=new EchoClient("192.168.49.93");
    private EchoClient echoClient3=new EchoClient("192.168.49.52");
//    private EchoClient echoClient4=new EchoClient("192.168.49.37");
//    private EchoClient echoClient5=new EchoClient("192.168.49.52");
//    private EchoClient echoClient6=new EchoClient("192.168.49.166");
    private boolean isgroupowner=DeviceDetailFragment.info.isGroupOwner;
    private  MulticastClient multicastClient=new MulticastClient();
    private Handler mVideoEncoderHandler;

    //This video stream format must be I420
    private final static ArrayBlockingQueue<byte []> mInputDatasQueue = new ArrayBlockingQueue<byte []>(CACHE_BUFFER_SIZE);
    //Cachhe video stream which has been encoded.
    public final static ArrayBlockingQueue<byte []> mOutputDatasQueue = new ArrayBlockingQueue<byte[]>(CACHE_BUFFER_SIZE);

//    private MediaCodec.Callback mCallback = ;

    public AsyncEncoder(String mimeType, int viewwidth, int viewheight){
        try {
            mMediaCodec = MediaCodec.createEncoderByType(mimeType);

        } catch (IOException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            mMediaCodec = null;
            return;
        }

        this.mViewWidth  = viewwidth;
        this.mViewHeight = viewheight;
        HandlerThread mVideoEncoderHandlerThread = new HandlerThread("VideoEncoder");
        mVideoEncoderHandlerThread.start();
        mVideoEncoderHandler = new Handler(mVideoEncoderHandlerThread.getLooper());


    }

    public void resetCodec(){

        mMediaCodec.reset();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setmMediaFormat(int bitrate, int fps) {

        mMediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, mViewWidth, mViewHeight);
        mMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);//视频格式
        mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate*1024);//码率1900000
        mMediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, fps);//帧率
        mMediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);//关键帧间隔
        mMediaFormat.setInteger(MediaFormat.KEY_BITRATE_MODE,MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR);
        try {
            mMediaFormat.setInteger(MediaFormat.KEY_PROFILE,MediaCodecInfo.CodecProfileLevel.AVCProfileMain);
            mMediaFormat.setInteger(MediaFormat.KEY_LEVEL,MediaCodecInfo.CodecProfileLevel.AVCProfileMain);
        }catch (Exception e){
            e.printStackTrace();
        }


        mMediaCodec.setCallback(new MediaCodec.Callback() {
            @Override
            public void onInputBufferAvailable(@NonNull MediaCodec mediaCodec, int id) {
//            ByteBuffer inputBuffer = mediaCodec.getInputBuffer(id);
//            inputBuffer.clear();
////            Log.d(TAG, "onInputBufferAvailable: 编码器编码输入缓冲区可用了！"+id);
//            byte [] dataSources = mInputDatasQueue.poll();//获取并移除此队列的头，如果是空队列则返回null
//            int length = 0;
//            if(dataSources != null) {
//                inputBuffer.put(dataSources);
//                length = dataSources.length;
////                Log.d(TAG, "onInputBufferAvailable: 元数据长度是："+length);
////                mediaCodec.queueInputBuffer(id,0, length,0,0);
//            }
//            mediaCodec.queueInputBuffer(id,0, length,System.nanoTime()/1000,0);
            }

            @Override
            public void onOutputBufferAvailable(@NonNull MediaCodec mediaCodec, int id, @NonNull MediaCodec.BufferInfo bufferInfo) {
                ByteBuffer outputBuffer = mMediaCodec.getOutputBuffer(id);
                MediaFormat outputFormat = mMediaCodec.getOutputFormat(id);
                if(outputBuffer != null && bufferInfo.size > 0){
                    byte [] buffer = new byte[outputBuffer.remaining()];
                    outputBuffer.get(buffer);
                    boolean result = mOutputDatasQueue.offer(buffer);//编好的数据进队
                    temp=mOutputDatasQueue.poll();
                    if (temp!=null){
                        try {
//                        echoClient.sendStream_n(temp,temp.length);
//                          echoClient2.sendStream_n(Util.rotateYUV240SP(temp,1440,1080),temp.length);
                            if (isgroupowner){
                            echoClient2.sendStream_n(temp,temp.length);
                            echoClient3.sendStream_n(temp,temp.length);
//                        echoClient4.sendStream_n(temp,temp.length);
//                        echoClient5.sendStream_n(temp,temp.length);
//                        echoClient6.sendStream_n(temp,temp.length);
//                        multicastClient.sendmessage(temp,temp.length);
//                        Log.d(TAG, "onOutputBufferAvailable: "+ Arrays.toString(temp));
                            }
//                        Log.d(TAG, "发送的数据"+(++number));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
//------------------------multicast------------------------------------------
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            try{multicastClient.sendmessage(temp,temp.length);}catch (IOException e){
//                                e.printStackTrace();
//                            }
//                        }
//                    }).start();
//                    Log.d(TAG, "onOutputBufferAvailable: 编码压缩后的数据"+temp.length);
                    }
                    else {
                        //Log.d(TAG, "onOutputBufferAvailable: 发送完毕！");
                    }
                    if(!result){
                        Log.d(TAG, "Offer to queue failed, queue in full state");
                    }
                }
                mMediaCodec.releaseOutputBuffer(id, true);
//            Log.d(TAG, "onOutputBufferAvailable: 释放了释放了"+number);
            }

            @Override
            public void onError(@NonNull MediaCodec mediaCodec, @NonNull MediaCodec.CodecException e) {
                Log.d(TAG, "------> onError");
            }

            @Override
            public void onOutputFormatChanged(@NonNull MediaCodec mediaCodec, @NonNull MediaFormat mediaFormat) {

                Log.d(TAG, "onOutputFormatChanged: "+mediaFormat.toString());
                Log.d(TAG, "------> onOutputFormatChanged");
            }
        }, mVideoEncoderHandler);
        mMediaCodec.configure(mMediaFormat, null, null, CONFIGURE_FLAG_ENCODE);
        mSurface=mMediaCodec.createInputSurface();


//        Log.d(TAG, "setmMediaFormat: 这是编码器返回的参数 "+mMediaCodec.getMetrics().toString());
    }

    /**
     * Input Video stream which need encode to Queue
     * @param needEncodeData I420 format stream
     */
    public void inputFrameToEncoder(byte [] needEncodeData){
        boolean inputResult = mInputDatasQueue.offer(needEncodeData);//数据进队
//        Log.d(TAG, "-----> inputEncoder queue result = " + inputResult + " queue current size = " + mInputDatasQueue.size());
    }

    /**
     * Get Encoded frame from queue
     * @return a encoded frame; it would be null when the queue is empty.
     */
    public byte [] pollFrameFromEncoder(){
        return mOutputDatasQueue.poll();
    }

    /**
     * start the MediaCodec to encode video data
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void startEncoder(){

        if(mMediaCodec != null){

            mMediaCodec.start();
            mMediaCodec.setVideoScalingMode(MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
        }else{
            throw new IllegalArgumentException("startEncoder failed,is the MediaCodec has been init correct?");
        }
    }
    private boolean ispps_sps(byte []data){
        if (data[0] == 0 && data[1] == 0 && data[2] == 0 && data[3] == 1 && data[4] == 103) {
            int j;
            for(j=0;j<data.length;j++){
                if (data[j]==104)
                    break;}
            mSps=new byte[j-4];
            mPps=new byte[data.length-(j-4)];
            System.arraycopy(data,0,mSps,0,j-4);
            System.arraycopy(data,j-4,mPps,0,data.length-(j-4));
        }
        return mPps != null && mSps != null;
    }
    /**
     * stop encode the video data
     */
    public void stopEncoder(){
        if(mMediaCodec != null){
            mMediaCodec.stop();
            mMediaCodec.setCallback(null);

            HandlerThread mVideoEncoderHandlerThread = new HandlerThread("VideoEncoder");
            mVideoEncoderHandlerThread.start();
            mVideoEncoderHandler = new Handler(mVideoEncoderHandlerThread.getLooper());


        }
    }
    /**
     * release all resource that used in Encoder
     */
    public void release(){
        if(mMediaCodec != null){
            mInputDatasQueue.clear();
            mOutputDatasQueue.clear();
            mMediaCodec.release();
        }
    }

}
