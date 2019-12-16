package com.example.dell.wi_fi_direct_based_videostream_ltf.UDP;

import android.os.Environment;
import android.os.Looper;
import android.util.Log;

import com.example.dell.wi_fi_direct_based_videostream_ltf.Coder.Frame;
import com.example.dell.wi_fi_direct_based_videostream_ltf.Coder.Object2Array;
import com.example.dell.wi_fi_direct_based_videostream_ltf.Multicast.MulticastClient;
import com.example.dell.wi_fi_direct_based_videostream_ltf.Packet.HeartBeatPacket;
import com.example.dell.wi_fi_direct_based_videostream_ltf.Packet.Packet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;

import static com.example.dell.wi_fi_direct_based_videostream_ltf.Camera.CameraActivity.TAG;

public class EchoServer implements Runnable {
    private EchoClient echoClient_multist=new EchoClient("192.168.49.166");
    private EchoClient echoClient = new EchoClient("192.168.49.28");
    private MulticastClient multicastClient = new MulticastClient();
    private DatagramSocket socket, socket1;

    private volatile boolean running;
    private byte[] buf = new byte[1024 * 90];

    private byte[] frame_fragment = new byte[60000];
    public DatagramPacket packet, heartbeat_packet;
    private final static int CACHE_BUFFER_SIZE = 180;//定义队列大小
    private final static ArrayBlockingQueue<byte[]> mInputDataQueue = new ArrayBlockingQueue<byte[]>(CACHE_BUFFER_SIZE);

    //    private final static ArrayBlockingQueue<byte[]>mOutputDataQueue=new ArrayBlockingQueue<byte[]>(CACHE_BUFFER_SIZE);
    public EchoServer() {
        try {
            socket = new DatagramSocket(4448);//接收视频
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        int packet_number = 0;
        Looper.prepare();
        running = true;
        while (running) {
            packet = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(packet);
                issplit(packet);
//                multicastClient.sendmessage(packet.getData(),packet.getLength());
//                echoClient.sendStream_n(packet.getData(), packet.getLength());
                //测试丢包专用
                packet_number++;
                //Log.d(TAG, " " + packet_number + "runEchoServer: " + Arrays.toString(packet.getData()));
//                echoClient_multist.sendStream_n(packet.getData(),packet.getData().length);
//                byte[] temp = new byte[packet.getLength()];
//
//                System.arraycopy(packet.getData(), 0, temp, 0, packet.getLength());
//                mInputDataQueue.offer(temp);
                //测试丢包专用
//                    Log.d(TAG, "run: 接收到了"+packet_number);
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

    private void issplit(DatagramPacket packet) {

        Frame frame = (Frame) Object2Array.byteArrayToObject(packet.getData());
        if (frame.issplit == 1 && frame.hasmore == 1) {
            System.arraycopy(frame.data, 0, frame_fragment, 0, frame.data.length);
        } else if (frame.issplit == 1 && frame.hasmore == 0) {
            byte[] frame_data = new byte[frame_fragment.length + frame.data.length];
            System.arraycopy(frame_fragment, 0, frame_data, 0, frame_fragment.length);
            System.arraycopy(frame.data, 0, frame_data, frame_fragment.length, frame.data.length);
            mInputDataQueue.offer(frame_data);//重组I帧并进队
        } else {

            mInputDataQueue.offer(frame.data);//直接拆包进队
        }
        //Log.d(TAG, "issplit: 队列空间的剩余大小"+mInputDataQueue.size());
    }

    public byte[] DecodePacket(byte[] data) {
        Frame frame = (Frame) Object2Array.byteArrayToObject(data);
        if (frame.issplit == 0)
            return frame.data;
        else {

            return frame_fragment;
        }
    }

    public byte[] pollFramedata() {

        if (mInputDataQueue.size() > 100)
            return mInputDataQueue.poll();
        return null;
    }

}
