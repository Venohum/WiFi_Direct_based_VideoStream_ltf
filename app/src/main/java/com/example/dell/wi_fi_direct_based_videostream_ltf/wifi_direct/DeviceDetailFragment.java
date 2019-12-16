package com.example.dell.wi_fi_direct_based_videostream_ltf.wifi_direct;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.dell.wi_fi_direct_based_videostream_ltf.R;
import com.example.dell.wi_fi_direct_based_videostream_ltf.chat.ChatActivity;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class DeviceDetailFragment extends Fragment implements WifiP2pManager.ConnectionInfoListener {
    private static final String TAG=DeviceDetailFragment.class.getSimpleName();
    protected static final int CHOOSE_FILE_RESULT_CODE = 20;
    private View mContentView = null;
    private WifiP2pDevice device;
    public static WifiP2pInfo info;
    ProgressDialog progressDialog = null;
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContentView = inflater.inflate(R.layout.device_detail, null);
        mContentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {//点击connect按钮

            @Override
            public void onClick(View v) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
               config.wps.setup = WpsInfo.PBC;//这到底是什么？
                /*WpsInfo是一个代表WiFi保护设置的类，而wps是WiFiP2pConfig的一个字段，字段类型就是WpsInfo，
                所以wps还可以继续调用WpsInfo中的方法steup，但是这个方法在API level 28中已经被弃用了。*/
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel",
                        "Connecting to :" + device.deviceAddress, true, true,new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                ((DeviceActionListener) getActivity()).cancelDisconnect();
                            }
                        }
                );
                ((DeviceActionListener) getActivity()).connect(config);
//                if (progressDialog != null && progressDialog.isShowing()) {
//                    progressDialog.dismiss();
//                }

            }
        });

        mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((DeviceActionListener) getActivity()).disconnect();
                    }
                });

        mContentView.findViewById(R.id.btn_start_client).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Allow user to pick an image from Gallery or other
                        // registered apps
//                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                        intent.setType("image/*");
//                        startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
                        Intent intent=new Intent(getActivity(),ChatActivity.class);
                        boolean data=info.isGroupOwner;
                        Log.d(TAG, "是组主么66666666"+data);
                        intent.putExtra("ChatActivity",data);
                        startActivity(intent);
                    }
                });

        return mContentView;
    }

    public void showDetails(WifiP2pDevice device) {
        this.device = device;
        this.getView().setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText("DeviceMacAddress:"+device.deviceAddress);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(device.toString());


    }
    /**
     * Clears the UI fields after a disconnect or direct mode disable operation.
     */
    public void resetViews() {
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.status_text);
        view.setText(R.string.empty);
       // mContentView.findViewById(R.id.btn_start_client).setVisibility(View.GONE);
        this.getView().setVisibility(View.GONE);
    }

     public void onConnectionInfoAvailable(final WifiP2pInfo info){
         if (progressDialog != null && progressDialog.isShowing()) {
             progressDialog.dismiss();
         }
         this.info = info;
         this.getView().setVisibility(View.VISIBLE);
     }

}
