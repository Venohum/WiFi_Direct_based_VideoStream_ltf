package com.example.dell.wi_fi_direct_based_videostream_ltf.Camera;
import android.media.MediaCodec;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.webkit.URLUtil;

import com.example.dell.wi_fi_direct_based_videostream_ltf.UDP.EchoClient;
import com.example.dell.wi_fi_direct_based_videostream_ltf.wifi_direct.DeviceDetailFragment;
import com.googlecode.javacv.cpp.opencv_nonfree;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.*;

import static com.example.dell.wi_fi_direct_based_videostream_ltf.Camera.CameraActivity.TAG;

/**
     * Created by litengfei on 2018/12/10.<br />
     */
public class Encoder {
        public static final int TRY_AGAIN_LATER = -1;
        public static final int BUFFER_OK = 0;
        public static final int BUFFER_TOO_SMALL = 1;
        public static final int OUTPUT_UPDATE = 2;

        private int format = 0;
        private final String MIME_TYPE = "video/avc";
        private MediaCodec mMC ;
        private MediaFormat mMF;
        public ByteBuffer[] inputBuffers;
        public ByteBuffer[] outputBuffers;
        private long BUFFER_TIMEOUT = 2000;
//        private MediaCodec.BufferInfo mBI;
        protected Surface surface;
        public  byte[] mPpsSps;
        public  byte[] mSps;
        public  byte[] mPps;
        private EchoClient echoClient=new EchoClient();

        private boolean isgroupowner=DeviceDetailFragment.info.isGroupOwner;
        public static File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/5.h264");

        /**
         * 初始化编码器
         * @throws IOException 创建编码器失败会抛出异常
         */

        public void init() throws IOException {
            mMC = MediaCodec.createEncoderByType(MIME_TYPE);
            /**
             * 这个暂时用在解析YUV_420_488格式的数据
             */
//            format = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible;
            /**
             * 这个暂时用在创建Surface作为编码器输入
             */
            format =MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;
//            mBI = new MediaCodec.BufferInfo();
        }
//        Encoder(Surface surface){
//            surface1=surface;
//        }
        /**
         * 配置编码器，需要配置颜色、帧率、比特率以及视频宽高
         * @param width 视频的宽
         * @param height 视频的高
         * @param bitrate 视频比特率
         * @param framerate 视频帧率
         */
        public void configure(int width, int height, int bitrate, int framerate){
            if(mMF == null){
                mMF = MediaFormat.createVideoFormat(MIME_TYPE, width, height);
                mMF.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
                mMF.setInteger(MediaFormat.KEY_FRAME_RATE, framerate);
                if (format != 0){
                    mMF.setInteger(MediaFormat.KEY_COLOR_FORMAT, format);
                }
                mMF.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1); //关键帧间隔时间 单位s
            }
            mMC.configure(mMF,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);

        }

        /**
         * 开启编码器，获取输入输出缓冲区
         */
        public void start(){
            /**
             *
             *这里注意，这个surface不能和dequeueInpputBuffer一同使用
             */
            surface=mMC.createInputSurface();
            mMC.start();

            inputBuffers = mMC.getInputBuffers();
            outputBuffers = mMC.getOutputBuffers();
        }

        /**
         * 向编码器输入数据，此处要求输入YUV420P的数据
         * @param data YUV数据
         * @param len 数据长度
         * @param timestamp 时间戳
         * @return
         */
        public int input(byte[] data,int len,long timestamp){
            /**
             * dequeueInputBuffer()函数解释
             * Returns the index of an input buffer to be filled with valid data or -1 if no such buffer is currently available.
             * This method will return immediately if timeoutUs == 0, wait indefinitely for the availability of an input buffer if timeoutUs < 0
             * or wait up to "timeoutUs" microseconds if timeoutUs > 0.
             * 返回buffer中的有效数据的索引，如果当前缓冲区不可用就返回-1，
             * 当timeoutUs为0时直接返回结果，当timeoutUs为小于0时，一直等待知道可用的input buffer出现为止
             *
             */
            int index = mMC.dequeueInputBuffer(-1);
            Log.v(TAG,"这是编码input的索引" + index+"   "+inputBuffers.length);
            if(index >= 0){
                ByteBuffer inputBuffer = inputBuffers[index];
                inputBuffer.clear();
                /**
                 *调用capacity方法，目的是返回缓冲区容量
                 */
//                if(inputBuffer.capacity() < len){
//                    mMC.queueInputBuffer(index, 0, 0, timestamp, 0);
//                    return BUFFER_TOO_SMALL;
//                }
                inputBuffer.put(data,0,len);
                mMC.queueInputBuffer(index,0,len,timestamp,0);
            }else{
                return index;
            }
            return BUFFER_OK;
        }

        /**
         * 输出编码后的数据
         */
        int output(){

            MediaCodec.BufferInfo mBI=new MediaCodec.BufferInfo();
            try{
            int i = mMC.dequeueOutputBuffer(mBI,0);
//            Log.d(TAG, "output: >>"+i);

//                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/5.h264");
//                BufferedOutputStream outputStream =new BufferedOutputStream(new FileOutputStream(file));
            while (i>=0) {
                byte[] data=new byte[mBI.size];
//                    outputBuffers[i].position(mBI.offset);
//                    outputBuffers[i].limit(mBI.offset + mBI.size);
                    outputBuffers[i].get(data, 0, mBI.size);
                 //Log.d(TAG, "output:haha "+Arrays.toString(data));

                if (data[0] == 0 && data[1] == 0 && data[2] == 0 && data[3] == 1 && data[4] == 103) {
                    mPpsSps=new byte[data.length];
                    mPpsSps = data;
                    int j;
                    for(j=0;i<mPpsSps.length;j++){
                        if (mPpsSps[j]==104)
                            break;}
                            mSps=new byte[j-4];mPps=new byte[mPpsSps.length-(j-4)];
                            System.arraycopy(mPpsSps,0,mSps,0,j-4);
                        System.arraycopy(mPpsSps,j-4,mPps,0,mPpsSps.length-(j-4));
                } else if (data[0] == 0 && data[1] == 0 && data[2] == 0 && data[3] == 1 && data[4] == 101) {
                    //在关键帧前面加上pps和sps数据
                    byte[] iframeData = new byte[mPpsSps.length + data.length];
                    System.arraycopy(mPpsSps, 0, iframeData, 0, mPpsSps.length);
                    System.arraycopy(data, 0, iframeData, mPpsSps.length, data.length);
                    data = iframeData;
                }
                Util.save(data, 0, data.length, file, true);
                if (!isgroupowner){
                echoClient.sendStream_n(data,data.length);

                }

                    mMC.releaseOutputBuffer(i,false);
                    i=mMC.dequeueOutputBuffer(mBI,0);
            }
            if (i == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                outputBuffers = mMC.getOutputBuffers();
                return OUTPUT_UPDATE;
            } else if (i == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                mMF = mMC.getOutputFormat();
                return OUTPUT_UPDATE;
            } else if (i == MediaCodec.INFO_TRY_AGAIN_LATER) {
                return TRY_AGAIN_LATER;
            }}catch (Exception e){
                e.printStackTrace();
            }
            return BUFFER_OK;
        }

        public void release(){
            mMC.stop();
            mMC.release();
            mMC = null;
            outputBuffers = null;
            inputBuffers = null;
        }
        public void flush() {
            mMC.flush();
        }
}
