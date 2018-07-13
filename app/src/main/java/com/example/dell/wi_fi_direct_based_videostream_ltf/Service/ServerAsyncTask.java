package com.example.dell.wi_fi_direct_based_videostream_ltf.Service;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import static com.example.dell.wi_fi_direct_based_videostream_ltf.chat.ChatActivity.TAG;

public class ServerAsyncTask extends AsyncTask<Integer,String,Void>{

    String msgFromClient=null;
    TextView textView=null;
    public ServerAsyncTask(TextView textView){
        this.textView=textView;
    }
    @Override
    protected Void doInBackground(Integer... integers){
    try{
        while(true){
            msgFromClient="";
            ServerSocket serverSocket=new ServerSocket(50000);
            Socket client=serverSocket.accept();
            if(client!=null){Log.d(TAG,"接收到了新的客户端");}
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(client.getInputStream()));
            String line;
            while((line=bufferedReader.readLine())!=null){
                msgFromClient=msgFromClient+line;
            }
            serverSocket.close();
            publishProgress(msgFromClient==null?"get nothing":msgFromClient);
        }
    }catch (Exception e){
        e.printStackTrace();
        Log.d(TAG,"出现异常！！连接不上");}

   return null; }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        textView.append(values.toString());
        Log.d(TAG,values.toString());

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }
}
