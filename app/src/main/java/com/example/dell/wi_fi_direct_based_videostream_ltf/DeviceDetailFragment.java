package com.example.dell.wi_fi_direct_based_videostream_ltf;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.view.View;
import android.widget.TextView;

public class DeviceDetailFragment extends Fragment {
    protected static final int CHOOSE_FILE_RESULT_CODE = 20;
    private View mContentView = null;
    private WifiP2pDevice device;
    private WifiP2pInfo info;
    ProgressDialog progressDialog = null;

    public void showDetails(WifiP2pDevice device) {
        this.device = device;
        this.getView().setVisibility(View.VISIBLE);
//        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
//        view.setText(device.deviceAddress);
//        view = (TextView) mContentView.findViewById(R.id.device_info);
//        view.setText(device.toString());

    }

}
