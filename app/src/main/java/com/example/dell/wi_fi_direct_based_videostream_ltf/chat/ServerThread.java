package com.example.dell.wi_fi_direct_based_videostream_ltf.chat;

import android.os.Message;
import android.util.Log;
import android.os.Handler;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class ServerThread implements Runnable {
    private Socket socket;
    private String type;
    private static ServerSocket serverSocket;
    private Handler myhandler;
    public ServerThread(String type,Handler handler){
    this.myhandler=handler;
    this.type=type;
    }
    @Override
    public void run(){
        try{
            if (serverSocket==null){
                serverSocket=new ServerSocket(50000);
                serverSocket.setReuseAddress(true);
            }
            Socket client=null;
            while(true){
                client=serverSocket.accept();
                if(client!=null){
                Log.d(ChatActivity.TAG,"新客户端"+client.getInetAddress().getHostAddress());}
                new Thread(new ServerRead(client)).start();

            }
        }catch (IOException e){
            e.printStackTrace();
        }

    }


    class ServerRead implements Runnable{
        private Socket socket;
        public ServerRead (Socket socket){
            this.socket=socket;
        }
        @Override
        public void run(){
            try{
                while(socket.isConnected()){
                    DataInputStream inputStream=new DataInputStream(socket.getInputStream());
                    Log.d(ChatActivity.TAG,"开始读");
                    String message =inputStream.readUTF();
                    if (message==null){
                        Log.d(ChatActivity.TAG, "message 是空的！ ");
                    }
                    Log.d(ChatActivity.TAG, message);
                    //Log.d(ChatActivity.TAG,"客户端"+socket.getInetAddress().getHostName()+"说:"+ message);
                    Message message1=Message.obtain();
                    message1.what=1;
                    message1.obj="客户端"+socket.getInetAddress().getHostName()+"说:"+ message;
                    myhandler.sendMessage(message1);
                }
            }catch (IOException e){
                e.printStackTrace();
            }
            finally {
                try {
                    if(socket != null && !socket.isClosed()){
                        socket.close();
                        System.out.println("socket 关闭");
                    }
                } catch (IOException e) {
                    Log.e(ChatActivity.TAG, "ServerThread 233");
                    e.printStackTrace();
                }
            }
        }
    }
    class ServerWrite implements Runnable{
        @Override
        public void run(){


        }
    }
}
