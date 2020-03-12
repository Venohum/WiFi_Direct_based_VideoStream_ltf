package com.example.dell.wi_fi_direct_based_videostream_ltf.Multicast;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Timer;

import static com.example.dell.wi_fi_direct_based_videostream_ltf.Camera.CameraActivity.TAG;

public class MulticastClient {

    private MulticastSocket multicastSocket;
    private byte[]buf;
    private static final int port = 50003;
    private  static final  int pp=88888;
    public MulticastClient(){
        try {
            multicastSocket=new MulticastSocket(port);

            Log.d(TAG, "MulticastClient: 我想看看这是谁的IP"+getIP());
            multicastSocket.setNetworkInterface(NetworkInterface.getByInetAddress(InetAddress.getByName(getIP())));
            multicastSocket.joinGroup(new InetSocketAddress(InetAddress.getByName("224.0.0.1"),port),NetworkInterface.getByName(getIP()));
            Log.d(TAG, "MulticastClient: first_commit");
            Log.d(TAG, "MulticastClient: second_test");
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void sendmessage(byte[]buf,int length)throws IOException{
        DatagramPacket datagramPacket=new DatagramPacket(buf,length,InetAddress.getByName("224.0.0.1"),port);
        multicastSocket.send(datagramPacket);
    }

    /**
     *
     * @return get the ip address of itself
     */
    public static String getIP(){

        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
                {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address))
                    {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        }
        catch (SocketException ex){
            ex.printStackTrace();
        }
        return null;
    }
}
