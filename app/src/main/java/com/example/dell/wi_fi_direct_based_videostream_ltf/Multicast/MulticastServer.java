package com.example.dell.wi_fi_direct_based_videostream_ltf.Multicast;

import android.os.Looper;
import android.util.Log;

import com.example.dell.wi_fi_direct_based_videostream_ltf.Coder.Frame;
import com.example.dell.wi_fi_direct_based_videostream_ltf.Coder.Object2Array;
import com.example.dell.wi_fi_direct_based_videostream_ltf.UDP.EchoClient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.concurrent.ArrayBlockingQueue;

import static com.example.dell.wi_fi_direct_based_videostream_ltf.Camera.CameraActivity.TAG;

public class MulticastServer implements Runnable {

    private int packet_number=0;
    private DatagramSocket socket;
    private MulticastSocket multicastSocket;
    private volatile boolean running;
    private byte[]buf=new byte[1024*40];
    public DatagramPacket packet;
    private static final int  port= 50003;
    //private byte[] buf = new byte[1024 * 90];

    private byte[] frame_fragment = new byte[60000];

    private final static int CACHE_BUFFER_SIZE=8;//定义队列大小
    private final static ArrayBlockingQueue<byte[]> mInputDataQueue=new ArrayBlockingQueue<byte[]>(CACHE_BUFFER_SIZE);
    //    private final static ArrayBlockingQueue<byte[]>mOutputDataQueue=new ArrayBlockingQueue<byte[]>(CACHE_BUFFER_SIZE);
    public MulticastServer(){
        try {
            multicastSocket=new MulticastSocket(port);
            Log.d(TAG, "MulticastServer: 本身的IP地址是"+getIP());
            multicastSocket.setNetworkInterface(NetworkInterface.getByInetAddress(InetAddress.getByName(getIP())));
            multicastSocket.joinGroup(new InetSocketAddress(InetAddress.getByName("224.0.0.1"),port),NetworkInterface.getByName(getIP()));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    @Override
    public void run() {

        Looper.prepare();
        running=true;
        while (running){
            packet=new DatagramPacket(buf,buf.length);
            try {
                multicastSocket.receive(packet);
//                new EchoClient("192.168.49.125").sendStream_n(packet.getData(),packet.getLength());
                issplit(packet);
                packet_number++;
                Log.d(TAG, " "+packet_number+"runMulticast: "+Arrays.toString(packet.getData()));
                byte[]temp=new byte[packet.getLength()];
                System.arraycopy(packet.getData(),0,temp,0,packet.getLength());
                mInputDataQueue.offer(temp);//数据进队
//                Log.d(TAG, "run: "+temp.length+"长"+Arrays.toString(mInputDataQueue.poll()));
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
    private  String getIP(){

        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
                {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address))
                    {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        }
        catch (SocketException ex){
            ex.printStackTrace();
        }
        return null;
    }
    public void stop_service(){

        running=false;
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
}
