package com.example.dell.wi_fi_direct_based_videostream_ltf.UDP;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class EchoClient {
    private DatagramSocket socket;
    private InetAddress address;
    private byte[] buf;

    public EchoClient(){
        try {
            socket=new DatagramSocket();
            address=InetAddress.getByName("192.168.49.1");
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
