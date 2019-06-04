package com.example.dell.wi_fi_direct_based_videostream_ltf.wifi_direct;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

import com.example.dell.wi_fi_direct_based_videostream_ltf.Algorithmic.ParametersCollection;
import com.example.dell.wi_fi_direct_based_videostream_ltf.R;

import static com.example.dell.wi_fi_direct_based_videostream_ltf.wifi_direct.WiFiDirectActivity.TAG;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private WiFiDirectActivity activity;
    final ParametersCollection parametersCollection;
    /**
     * @param manager WifiP2pManager system service
     * @param channel Wifi p2p channel
     * @param activity activity associated with the receiver
     */
    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, WiFiDirectActivity activity) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
        parametersCollection=new ParametersCollection(activity);
    }
    /*
     * (non-Javadoc)
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
     * android.content.Intent)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            // UI update to indicate wifi p2p status.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi Direct mode is enabled
                activity.setIsWifiP2pEnabled(true);
            } else {
                activity.setIsWifiP2pEnabled(false);
                activity.resetData();

            }
            Log.d(TAG, "P2P state changed - " + state);
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            Toast.makeText(activity,"WiFi_P2P_PEERS_CHANGED_ACTION",Toast.LENGTH_SHORT).show();
            Log.d(TAG, "WiFi_P2P_PEERS_CHANGED_ACTION");
            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (manager != null) {
                manager.requestPeers(channel, (WifiP2pManager.PeerListListener) activity.getFragmentManager()
                        .findFragmentById(R.id.frag_list));
            }
            Log.d(TAG, "P2P peers changed");
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {



            if (manager == null) {
                return;
            }
            Toast.makeText(activity,"P2P_connection_changed_action",Toast.LENGTH_SHORT).show();
            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {

                // we are connected with the other device, request connection
                // info to find group owner IP

                DeviceDetailFragment fragment = (DeviceDetailFragment) activity.getFragmentManager().findFragmentById(R.id.frag_detail);
              manager.requestConnectionInfo(channel, fragment);
              manager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {
                  @Override
                  public void onGroupInfoAvailable(WifiP2pGroup group) {
                      if (group!=null){
                          Log.d(TAG, "onGroupInfoAvailable: "+group.getPassphrase());
                          activity.setSSID(group.getNetworkName());
                          parametersCollection.setStatus(true);
                          if (!DeviceDetailFragment.info.isGroupOwner)
                          new Thread(parametersCollection).start();
                      }
                  }
              });
            } else {
                // It's a disconnect
                parametersCollection.setStatus(false);
                activity.resetData();
//                Log.d(TAG, "onReceive: 断了断了");
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager()
                    .findFragmentById(R.id.frag_list);
            fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
            Log.d(TAG, "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION+last");
            Toast.makeText(activity,"Device_changed_action",Toast.LENGTH_SHORT).show();
        }
    }
}
