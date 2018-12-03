package com.example.dell.wi_fi_direct_based_videostream_ltf.Service;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import static com.example.dell.wi_fi_direct_based_videostream_ltf.chat.ChatActivity.TAG;

public class ServerAsyncTask extends AsyncTask<Integer,String,Void>{

//    private String msgFromClient=null;
    String s;
    private TextView textView;
    public ServerAsyncTask(TextView textView){
        this.textView=textView;
    }
    @Override
    protected Void doInBackground(Integer... integers){
    try{
        while(true){
//            msgFromClient="";
            ServerSocket serverSocket=new ServerSocket(50000);
            Socket client=serverSocket.accept();
            if(client!=null){
                Log.d(TAG,"接收到了新的客户端");
                BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(client.getInputStream()));

            String line;
            while((line=bufferedReader.readLine())!=null){
//                msgFromClient=msgFromClient+line;
                 s=s+line;
            }
            serverSocket.close();
            publishProgress(s==null?"get nothing":s);
                Log.d(TAG, "doInBackground: "+s);
            }
        }
    }catch (Exception e){
        e.printStackTrace();
        Log.d(TAG,"出现异常！！连接不上");}

   return null; }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        String s_textView=values[0];
        textView.setText(s_textView);
        Log.d(TAG, String.valueOf(values[0]));

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }
}
