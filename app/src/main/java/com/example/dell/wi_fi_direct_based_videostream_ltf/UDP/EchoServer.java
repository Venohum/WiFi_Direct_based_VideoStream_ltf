package com.example.dell.wi_fi_direct_based_videostream_ltf.UDP;

import android.os.Environment;
import android.os.Looper;
import android.util.Log;

import com.example.dell.wi_fi_direct_based_videostream_ltf.Multicast.MulticastClient;

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
//    private EchoClient echoClient_multist=new EchoClient("192.168.49.166");
    private EchoClient echoClient=new EchoClient("192.168.49.28");
    private MulticastClient multicastClient=new MulticastClient();
    private DatagramSocket socket;

    private boolean running;
    private byte[]buf=new byte[1024*90];
    private FileOutputStream fileOutputStream;
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
        int packet_number=0;
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
//                multicastClient.sendmessage(packet.getData(),packet.getLength());
//                echoClient.sendStream_n(packet.getData(), packet.getLength());
                //测试丢包专用
                packet_number++;
                Log.d(TAG, " "+packet_number+"runEchoServer: "+Arrays.toString(packet.getData()));
//                echoClient_multist.sendStream_n(packet.getData(),packet.getData().length);
                byte[]temp=new byte[packet.getLength()];
                System.arraycopy(packet.getData(),0,temp,0,packet.getLength());
                mInputDataQueue.offer(temp);
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
    public byte[] pollFramedata(){
            return mInputDataQueue.poll();
    }

}
