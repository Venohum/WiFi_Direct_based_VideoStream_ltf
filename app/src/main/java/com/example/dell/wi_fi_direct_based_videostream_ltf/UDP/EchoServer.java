package com.example.dell.wi_fi_direct_based_videostream_ltf.UDP;

import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.dell.wi_fi_direct_based_videostream_ltf.Camera.Decoder;
import com.example.dell.wi_fi_direct_based_videostream_ltf.Camera.Encoder;
import com.example.dell.wi_fi_direct_based_videostream_ltf.R;
import com.googlecode.javacv.FrameGrabber;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;

import static com.example.dell.wi_fi_direct_based_videostream_ltf.Camera.CameraActivity.TAG;

public class EchoServer implements Runnable {

    private DatagramSocket socket;
    private boolean running;
    private byte[]buf=new byte[1024*40];
    private FileOutputStream fileOutputStream;
    private Decoder mDecoder;
    private Encoder encoder;
    public DatagramPacket packet;
    private final static int CACHE_BUFFER_SIZE=8;//定义队列大小
    private final static ArrayBlockingQueue<byte[]>mInputDataQueue=new ArrayBlockingQueue<byte[]>(CACHE_BUFFER_SIZE);
//    private final static ArrayBlockingQueue<byte[]>mOutputDataQueue=new ArrayBlockingQueue<byte[]>(CACHE_BUFFER_SIZE);
    public EchoServer(){
        try {
            socket=new DatagramSocket(4448);
        }catch (SocketException e){
            e.printStackTrace();
        }

    }
    @Override
    public void run() {

        Looper.prepare();
        try  {
            fileOutputStream = new FileOutputStream(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/999.h264"));
        }catch (IOException e){
            e.printStackTrace();
        }
        running=true;
        while (running){
             packet=new DatagramPacket(buf,buf.length);
            try {
                socket.receive(packet);
                byte[]temp=new byte[packet.getLength()];
                System.arraycopy(packet.getData(),0,temp,0,packet.getLength());
                mInputDataQueue.offer(temp);
//                Log.d(TAG, "run: "+temp.length+"长"+Arrays.toString(mInputDataQueue.poll()));
//                fileOutputStream.write(temp);
            } catch (IOException e) {
                e.printStackTrace();
            }
//            InetAddress address=packet.getAddress();
//            Log.d(TAG, "run: 发送端地址是"+address);
//            int port =packet.getPort();
//            packet=new DatagramPacket(buf,buf.length,port);
//            String received=new String(packet.getData(),0,packet.getLength());
//            Log.d(TAG, "UDP的接收端run: " +received);
//            if (received.equals("end")){
//                running =false;
//                continue;
//            }
//            try {
//                socket.send(packet);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
        socket.close();
        Looper.loop();
    }
    public byte[] pollFramedata(){
            return mInputDataQueue.poll();
    }

}
