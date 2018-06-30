package com.example.dell.wi_fi_direct_based_videostream_ltf.chat;

import android.nfc.Tag;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.dell.wi_fi_direct_based_videostream_ltf.R;

public class ChatActivity extends AppCompatActivity {
    ClientThread clientThread=null;
    ServerThread serverThread=null;
   Boolean bool=true;
    Message message;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg){
            TextView text_view=(TextView)findViewById(R.id.chat_content);
            switch (msg.what){
                case 1:
                    text_view.append((String)msg.obj+"\n");
                    break;
            }
    }
};
    public static final String TAG = "ChatAcyivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        final EditText editText=(EditText)findViewById(R.id.Sendtext);
        Button button=(Button)findViewById(R.id.Sendmessage);
        if (serverThread==null){
        serverThread=new ServerThread("read",handler);
        new Thread(serverThread).start();
            Log.d(ChatActivity.TAG,"线程第一次被创建");}
        if (clientThread==null){
        clientThread=new ClientThread("write",handler);
        new Thread(clientThread).start();}



//        while(bool){
//            if(clientThread.myhandler!=null) {
//            clientThread.myhandler.sendMessage(message);
//            bool=false;}
//        }
//        Log.d(ChatActivity.TAG,"halellelelellelel");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message=Message.obtain();
                message.what=345;
                message.obj=editText.getText().toString();
                editText.setText("");
                if (clientThread.myhandler!=null){
                    clientThread.myhandler.sendMessage(message);
                }
            }
        });
    }
}
