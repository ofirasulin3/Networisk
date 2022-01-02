package com.example.networisk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

class WifiReceiver extends BroadcastReceiver {
    WifiManager wifiManager;
    StringBuilder sb;
    ListView wifiDeviceList;
    public WifiReceiver(WifiManager wifiManager, ListView wifiDeviceList) {
        this.wifiManager = wifiManager;
        this.wifiDeviceList = wifiDeviceList;
    }
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {

            //0. first, we need to make a button
            // "To set current location as the focal point, press on this button:
            //1. Extract current GPS Location
            //2. Check if it's in a given radius (can calculate distance like we did in rides app)
            //3. Check how to collect in the background..



            //sb = new StringBuilder();
            List<ScanResult> wifiList = wifiManager.getScanResults();
            ArrayList<String> deviceList = new ArrayList<>();
            for (ScanResult scanResult : wifiList) {
                long microseconds = scanResult.timestamp;
                long days = TimeUnit.MICROSECONDS.toDays(microseconds);
                deviceList.add("                      " + scanResult.SSID
                        + "\nCapabilities: " + scanResult.capabilities
                        + "\nBSSID: " + scanResult.BSSID
                        + "\nLevel: " + scanResult.level
                        + "\nTimestamp: " + days +" days"
                        + "\nVenue Name: " + scanResult.venueName// This was deprecated in API 31
                        + "\nPasspoint friendly name: " + scanResult.operatorFriendlyName //newer name=getPasspointProviderFriendlyName
                        + "\nIs it passpoint? " + scanResult.isPasspointNetwork());
            }
            //Toast.makeText(context, sb, Toast.LENGTH_SHORT).show();
            ArrayAdapter arrayAdapter = new ArrayAdapter(context, android.R.layout.simple_list_item_1, deviceList.toArray());
            wifiDeviceList.setAdapter(arrayAdapter);
        }
    }
}