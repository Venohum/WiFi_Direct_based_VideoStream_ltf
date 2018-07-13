package com.example.dell.wi_fi_direct_based_videostream_ltf.chat;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.nfc.Tag;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.dell.wi_fi_direct_based_videostream_ltf.R;
import com.example.dell.wi_fi_direct_based_videostream_ltf.Service.ServerAsyncTask;
import com.example.dell.wi_fi_direct_based_videostream_ltf.Service.ClientAsynTask;
import com.example.dell.wi_fi_direct_based_videostream_ltf.recorder.RecordService;
import com.example.dell.wi_fi_direct_based_videostream_ltf.recorder.ScreenRecordActivity;
import com.example.dell.wi_fi_direct_based_videostream_ltf.wifi_direct.DeviceDetailFragment;

public class ChatActivity extends AppCompatActivity {
    WifiP2pInfo wifiP2pInfo;
    WifiP2pManager wifiP2pManager;
    ClientThread clientThread=null;
    ServerThread serverThread=null;
   Boolean bool=true;
   Boolean isgroupowner;
    private static final int RECORD_REQUEST_CODE  = 101;
    private static final int STORAGE_REQUEST_CODE = 102;
    private static final int AUDIO_REQUEST_CODE   = 103;
    private MediaProjectionManager projectionManager;
    private MediaProjection mediaProjection;
    private RecordService recordService;
    private Button startBtn;

//    Message message;
//    private Handler handler=new Handler(){
//        @Override
//        public void handleMessage(Message msg){
//            TextView text_view=(TextView)findViewById(R.id.chat_content);
//            switch (msg.what){
//                case 1:
//                    text_view.append((String)msg.obj+"\n");
//                    break;
//            }
//    }
//};
    public static final String TAG = "ChatAcyivity";
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        setContentView(R.layout.activity_chat);
//        wifiP2pInfo=new WifiP2pInfo();
//        if (wifiP2pInfo!=null){
//        Log.d(TAG, "哈哈哈哈哈,P2p地址是kong "+wifiP2pInfo.toString() );}

        Intent intent1=getIntent();
        isgroupowner=intent1.getBooleanExtra("ChatActivity",true);
        wifiP2pManager=(WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        WifiManager wifiManager=(WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        final EditText editText=(EditText)findViewById(R.id.Sendtext);
        Button button=(Button)findViewById(R.id.Sendmessage);
        ServerAsyncTask serverAsyncTask=new ServerAsyncTask(editText);
        ClientAsynTask clientAsynTask=new ClientAsynTask("成功");
//        clientAsynTask.execute("192.168.49.1","5000","成功");
        if (isgroupowner==true){
        serverAsyncTask.execute(50000);}
       else {
            clientAsynTask.execute("成功");
        }
        /*
        if (serverThread==null){
        serverThread=new ServerThread("read",handler);
        new Thread(serverThread).start();
            Log.d(ChatActivity.TAG,"线程第一次被创建");}
        if (clientThread==null){
        clientThread=new ClientThread("write",handler);
        new Thread(clientThread).start();}*/

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                message=Message.obtain();
//                message.what=345;
//                message.obj=editText.getText().toString();
//                editText.setText("");
//                if (clientThread.myhandler!=null){
//                    clientThread.myhandler.sendMessage(message);
//                }
            }
        });
        startBtn = (Button) findViewById(R.id.start_record);
//        startBtn.setEnabled(false);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                if (recordService.isRunning()) {
                    recordService.stopRecord();
                    startBtn.setText(R.string.start_record);
                } else {
                    Intent captureIntent = projectionManager.createScreenCaptureIntent();
                    startActivityForResult(captureIntent, RECORD_REQUEST_CODE);
                }
            }
        });
        if (ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_REQUEST_CODE);
        }

        if (ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.RECORD_AUDIO}, AUDIO_REQUEST_CODE);
        }
        Intent intent = new Intent(this, RecordService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECORD_REQUEST_CODE && resultCode == RESULT_OK) {
            mediaProjection = projectionManager.getMediaProjection(resultCode, data);
            recordService.setMediaProject(mediaProjection);
            recordService.startRecord();
            startBtn.setText(R.string.stop_record);
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == STORAGE_REQUEST_CODE || requestCode == AUDIO_REQUEST_CODE) {
//            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//                finish();
//            }
//        }
//    }
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            RecordService.RecordBinder binder = (RecordService.RecordBinder) service;
            recordService = binder.getRecordService();
            recordService.setConfig(metrics.widthPixels, metrics.heightPixels, metrics.densityDpi);
            startBtn.setEnabled(true);
            startBtn.setText(recordService.isRunning() ? R.string.stop_record : R.string.start_record);
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {}
    };
}

