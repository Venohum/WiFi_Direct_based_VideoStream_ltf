package com.example.dell.wi_fi_direct_based_videostream_ltf.Camera;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static com.example.dell.wi_fi_direct_based_videostream_ltf.Camera.CameraActivity.TAG;


/**
 * Created by Litengfei on 2018/12/10.<br />
 */

public class Decoder {

    public static final int TRY_AGAIN_LATER = -1;
    public static final int BUFFER_OK = 0;
    public static final int BUFFER_TOO_SMALL = 1;
    public static final int OUTPUT_UPDATE = 2;

    private final String MIME_TYPE = "video/avc";
    private MediaCodec mMC = null;
    private MediaFormat mMF;
    private long BUFFER_TIMEOUT = 1000;
    private MediaCodec.BufferInfo mBI;
    private ByteBuffer[] mInputBuffers;
    private ByteBuffer[] mOutputBuffers;
    private boolean mStopFlag=false;
    private byte[] dummyFrame = new byte[]{0x00, 0x00, 0x01, 0x20};
    private byte[] streamBuffer = null;
    private byte[] marker0 = new byte[]{0, 0, 0, 1};

    /**
     * 初始化编码器
     * @throws IOException 创建编码器失败会抛出异常
     */
    public void init() throws IOException {
        mMC = MediaCodec.createDecoderByType(MIME_TYPE);
//        mBI = new MediaCodec.BufferInfo();
    }



    /**
     * 配置解码器
     * @param sps 用于配置的sps参数
     * @param pps 用于配置的pps参数
     * @param surface 用于解码显示的Surface
     */
    public void configure(byte[] sps,byte[] pps,Surface surface){
        int[] width = new int[1];
        int[] height = new int[1];
//        int width=
        //AvcUtils.parseSPS(sps, width, height);//从sps中解析出视频宽高

//        byte[] sps = {0, 0, 0, 1, 103, 66, 0, 42, (byte) 149, (byte) 168, 30, 0, (byte) 137, (byte) 249, 102, (byte) 224, 32, 32, 32, 64};
//        byte[] pps = {0, 0, 0, 1, 104, (byte) 206, 60, (byte) 128, 0, 0, 0, 1, 6, (byte) 229, 1, (byte) 151, (byte) 128};
        width[0] = (H264SPSPaser.ue(sps,34) + 1)*16;
        height[0] = (H264SPSPaser.ue(pps,-1) + 1)*16;
        Log.d(TAG, "configure: 宽是"+width[0]);
        Log.d(TAG, "configure: 高是"+height[0]);

        mMF = MediaFormat.createVideoFormat(MIME_TYPE, width[0], height[0]);
        mMF.setByteBuffer("csd-0", ByteBuffer.wrap(sps));
        mMF.setByteBuffer("csd-1", ByteBuffer.wrap(pps));
        mMF.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, width[0]* height[0]);
        mMC.configure(mMF, surface, null, 0);
    }

    /**
     * 开启解码器，获取输入输出缓冲区
     */
    public void start(){
        mMC.start();
        mInputBuffers = mMC.getInputBuffers();
        mOutputBuffers = mMC.getOutputBuffers();
    }

    /**
     * 输入数据
     * @param data 输入的数据
     * @param len 数据有效长度
     * @param timestamp 时间戳
     * @return 成功则返回{@link #BUFFER_OK} 否则返回{@link #TRY_AGAIN_LATER}
     */
    public int input(byte[] data,int len,long timestamp){
        mBI = new MediaCodec.BufferInfo();
        long startMs = System.currentTimeMillis();
//        int i = mMC.dequeueInputBuffer(BUFFER_TIMEOUT);
//        if(i >= 0){
//            ByteBuffer inputBuffer = mInputBuffers[i];
//            inputBuffer.clear();
//            inputBuffer.put(data,0, len);
//            mMC.queueInputBuffer(i, 0, len, timestamp, 0);
//        }else {
//            return TRY_AGAIN_LATER;
//        }
        try {
            //获取文件输入流
//            DataInputStream mInputStream = new DataInputStream(new FileInputStream(Encoder.file));
//            streamBuffer = getBytes(mInputStream);
            streamBuffer=data;
           // Log.d(TAG, "input: "+streamBuffer.length);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int bytes_cnt;
        while (!mStopFlag ) {
            bytes_cnt = streamBuffer.length;
            if (bytes_cnt == 0) {
                streamBuffer = dummyFrame;
            }

            int startIndex = 0;
            int remaining = bytes_cnt;
            while (true) {
                if (remaining == 0 || startIndex >= remaining) {
                    break;
                }
                int nextFrameStart = KMPMatch(marker0, streamBuffer, startIndex + 2, remaining);//返回下一帧开始的索引
                Log.d(TAG, "input nextFrameStart:"+nextFrameStart);
                if (nextFrameStart == -1) {
                    nextFrameStart = remaining;
                    Log.d(TAG, "input: remaining"+remaining+"remaining 给了nextFrameStart!");
                }

                int inIndex = mMC.dequeueInputBuffer(BUFFER_TIMEOUT);
                if (inIndex >= 0) {
                    ByteBuffer byteBuffer = mInputBuffers[inIndex];
                    byteBuffer.clear();
                    Log.d(TAG, "input: byteBuffer调用put之前"+byteBuffer.toString());
                    byteBuffer.put(streamBuffer, startIndex, nextFrameStart - startIndex);
                    Log.d(TAG, "input: byteBuffer调用put之后"+byteBuffer.toString());
                    //在给指定Index的inputbuffer[]填充数据后，调用这个函数把数据传给解码器
                    mMC.queueInputBuffer(inIndex, 0, nextFrameStart - startIndex, 0, 0);
                    startIndex = nextFrameStart;
                } else {
                    Log.e(TAG, "aaaaa");
                    continue;
                }
                try {
                    //获取文件输入流
                    DataInputStream mInputStream = new DataInputStream(new FileInputStream(Encoder.file));
                    streamBuffer = getBytes(mInputStream);
                    Log.d(TAG, "input: "+streamBuffer.length);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                int outIndex = mMC.dequeueOutputBuffer(mBI, BUFFER_TIMEOUT);
                Log.d(TAG, "input: outIndex:"+outIndex);
                if (outIndex >= 0) {
                    Log.d(TAG, "input: System.curentTimeMillis():"+System.currentTimeMillis());
                    Log.d(TAG, "input: startMs:"+startMs);
                    Log.d(TAG, "input: System.currentTimeMillis()-startMs:"+(System.currentTimeMillis()-startMs));
                    Log.d(TAG, "input: presentationTimeUs:"+mBI.presentationTimeUs);
//                    帧控制是不在这种情况下工作，因为没有PTS H264是可用的
                    while (mBI.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
                        try {
                            Thread.sleep(100);
                            Log.d(TAG, Thread.currentThread().getName()+"睡了100ms！");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    boolean doRender = (mBI.size != 0);
                    Log.d(TAG, "input: doRender:"+doRender+"  mBI.size:"+mBI.size+" mBI.offset:"+mBI.offset);

                    //对outputbuffer的处理完后，调用这个函数把buffer重新返回给codec类。
                    mMC.releaseOutputBuffer(outIndex, doRender);
                }
//                else {
//                    Log.e(TAG, "bbbb");
//                }
            }
            mStopFlag = true;
        }

        return BUFFER_OK;

    }

    public int output(byte[] data,int[] len,long[] ts){

        int i = mMC.dequeueOutputBuffer(mBI, BUFFER_TIMEOUT);
        if(i >= 0){
            if (mOutputBuffers[i] != null)
            {
                mOutputBuffers[i].position(mBI.offset);
                mOutputBuffers[i].limit(mBI.offset + mBI.size);

                if (data != null)
                    mOutputBuffers[i].get(data, 0, mBI.size);
                len[0] = mBI.size;
                ts[0] = mBI.presentationTimeUs;
            }
            mMC.releaseOutputBuffer(i, true);
        }else{
            return TRY_AGAIN_LATER;
        }
        return BUFFER_OK;
    }

    public void flush(){
        mMC.flush();
    }

    public void release() {
        flush();
        mMC.stop();
        mMC.release();
        mMC = null;
        mInputBuffers = null;
        mOutputBuffers = null;
    }

    private int KMPMatch(byte[] pattern, byte[] bytes, int start, int remain) {
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int[] lsp = computeLspTable(pattern);

        int j = 0;  // Number of chars matched in pattern
        for (int i = start; i < remain; i++) {
            while (j > 0 && bytes[i] != pattern[j]) {
                // Fall back in the pattern
                j = lsp[j - 1];  // Strictly decreasing
            }
            if (bytes[i] == pattern[j]) {
                // Next char matched, increment position
                j++;
                if (j == pattern.length)
                    return i - (j - 1);
            }
        }
        return -1;  // Not found
    }
    private int[] computeLspTable(byte[] pattern) {
        int[] lsp = new int[pattern.length];
        lsp[0] = 0;  // Base case
        for (int i = 1; i < pattern.length; i++) {
            // Start by assuming we're extending the previous LSP
            int j = lsp[i - 1];
            while (j > 0 && pattern[i] != pattern[j])
                j = lsp[j - 1];
            if (pattern[i] == pattern[j])
                j++;
            lsp[i] = j;
        }
        return lsp;
    }
    private static byte[] getBytes(InputStream is) throws IOException {
        int len;
        int size = 10*1024;
        byte[] buf;
        if (is instanceof ByteArrayInputStream) {
            size = is.available();
            buf = new byte[size];
//            len = is.read(buf, 0, size);
        } else {
//            BufferedOutputStream bos=new BufferedOutputStream(new ByteArrayOutputStream());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            buf = new byte[size];
            while ((len = is.read(buf, 0, size)) != -1)
                bos.write(buf, 0, len);
            buf = bos.toByteArray();
        }
        Log.e(TAG, "bbbb");
        return buf;
    }



}
