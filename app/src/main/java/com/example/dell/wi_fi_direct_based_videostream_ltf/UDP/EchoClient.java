package com.example.dell.wi_fi_direct_based_videostream_ltf.UDP;

import android.content.Context;
import android.util.Log;

import com.example.dell.wi_fi_direct_based_videostream_ltf.Coder.Frame;
import com.example.dell.wi_fi_direct_based_videostream_ltf.Coder.Object2Array;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Enumeration;

import static com.example.dell.wi_fi_direct_based_videostream_ltf.Camera.CameraActivity.TAG;

public class EchoClient {
    private DatagramSocket socket;
    private MulticastSocket multicastSocket;
    private InetAddress address;
    private Frame frame1=new Frame();
    private Frame frame2=new Frame();
    private byte[] buf;
//
    public EchoClient(String ipaddress){
        try {
            socket=new DatagramSocket();
            address=InetAddress.getByName(ipaddress);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public EchoClient(String ipaddress,boolean setnetworkInterFace){
        try {
            socket=new DatagramSocket(null);
            address= InetAddress.getByName(ipaddress);
            InetAddress inetAddress =InetAddress.getLocalHost();
            Log.d(TAG, "EchoClient:主机名是 "+inetAddress.getHostName()+"是否绑定了"+socket.isBound()+"shifo"+socket.isClosed());
            if (setnetworkInterFace){
                socket.bind(new InetSocketAddress(InetAddress.getByName("192.168.49.99"),4448));
            Log.d(TAG, "EchoClient: 执行了绑定过程！"+socket.isClosed());}
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void sendEcho(String msg)throws IOException{
        buf=msg.getBytes();
        DatagramPacket packet=new DatagramPacket(buf,buf.length,address,4448);
        socket.send(packet);
    }
    public String received()throws IOException{
        byte[]buf =new byte[1024];
        DatagramPacket packet=new DatagramPacket(buf,buf.length);
        socket.receive(packet);
        return new String(packet.getData(),0,packet.getLength());
    }

    public void sendStream(byte[]buf,int length) throws IOException {
        int i=0;
        byte[]temp=new byte[length];
        while (i<=buf.length/length){
            if (i<buf.length/length)
                try{
            System.arraycopy(buf,i*length,temp,0,length);}catch (Exception e){
                e.printStackTrace();
                }
            if (i==buf.length/length)
                System.arraycopy(buf,i*1024,temp,0,buf.length-i*length);
            DatagramPacket datagramPacket=new DatagramPacket(temp,temp.length,address,4448);
            socket.send(datagramPacket);
            i++;

        }
    }
    public void sendStream_n (byte[]buf,int length)throws IOException {

        if (buf.length>65507){
            Log.d(TAG, "sendStream_n: Message too long！I帧超长，进行分割，"+buf.length);
            byte [] fragment1=new byte[60000];
            byte [] fragment2=new byte[length-60000];
            System.arraycopy(buf,0,fragment1,0,60000);
            System.arraycopy(buf,60000,fragment2,0,length-60000);
            frame1.data=fragment1;
            frame1.issplit=1;
            frame1.hasmore=1;
            frame2.data=fragment2;
            frame2.hasmore=0;
            frame2.issplit=1;
            DatagramPacket datagramPacket1=new DatagramPacket(Object2Array.objectToByteArray(frame1),Object2Array.objectToByteArray(frame1).length,address,4448);
            DatagramPacket datagramPacket2=new DatagramPacket(Object2Array.objectToByteArray(frame2),Object2Array.objectToByteArray(frame2).length,address,4448);
            socket.send(datagramPacket1);
            socket.send(datagramPacket2);
        }
        else {
            Frame frame=new Frame();
            frame.data=buf;
            frame.issplit=0;
            frame.hasmore=0;
        DatagramPacket datagramPacket=new DatagramPacket(Object2Array.objectToByteArray(frame),Object2Array.objectToByteArray(frame).length,address,4448);
        socket.send(datagramPacket);
        }
    }
    public void close(){
        socket.close();
    }

}
