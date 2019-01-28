package com.example.dell.wi_fi_direct_based_videostream_ltf.UDP;

import android.content.Context;
import android.util.Log;

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
    private byte[] buf;

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
            socket=new DatagramSocket();
            address= InetAddress.getByName(ipaddress);
            InetAddress inetAddress =InetAddress.getLocalHost();
            Log.d(TAG, "EchoClient:主机名是 "+inetAddress.getHostName());
            if (setnetworkInterFace)
                socket.bind(new InetSocketAddress(InetAddress.getByName("192.168.49.99"),4448));
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

        DatagramPacket datagramPacket=new DatagramPacket(buf,length,address,4448);
        socket.send(datagramPacket);
    }

    public void close(){
        socket.close();

    }

}
