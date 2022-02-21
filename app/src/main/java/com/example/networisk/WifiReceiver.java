package com.example.networisk;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.amplifyframework.core.Amplify;
import com.amplifyframework.storage.StorageAccessLevel;
import com.amplifyframework.storage.options.StorageUploadFileOptions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

class WifiReceiver extends BroadcastReceiver {
    SharedPreferences sharedPref;
    WifiManager wifiManager;
    ListView wifiDeviceList;
//    Location focalLocation;
//    Location currentLocation;

//    public static int getFileCounter() {
//        return fileCounter;
//    }
//
//    static int fileCounter = 1;
    //StringBuilder sb;

    public WifiReceiver(WifiManager wifiManager, ListView wifiDeviceList, SharedPreferences sharedPref) {
        this.wifiManager = wifiManager;
        this.wifiDeviceList = wifiDeviceList;
        this.sharedPref = sharedPref;
//        this.focalLocation = FocalLocation;
//        this.currentLocation = CurrentLocation;
    }

    public String getGUID() {
        String GUID = sharedPref.getString("guid", "-1");
        if(GUID.equals("-1")) {
            SharedPreferences.Editor editor = sharedPref.edit();
            GUID = UUID.randomUUID().toString();
            editor.putString("guid", GUID);
            editor.apply();
        }
        return GUID;
    }

    public int getFileCounter() {
        int fileCounter = sharedPref.getInt("fileCounter", 0) + 1;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("fileCounter", fileCounter);
        editor.apply();
        return fileCounter;
    }

    public void onReceive(Context context, Intent intent) {
        //calculating distance (in meters) between currentLocation and focalLocation
//        if(focalLocation!=null && currentLocation!=null){
            //float dist = focalLocation.distanceTo(currentLocation);

            //Check if it's in a given radius
            //if(dist>=500){

        if(MainActivity.getInside()==0){
            //Log.i("Prints", "currentLocation far from FocalLocation so doesn't update list.");
            Log.i("Prints", "Outside Geofence");
            return;
        }
        else {
            //Log.i("Prints", "currentLocation is within radius");
            Log.i("Prints", "Inside Geofence");

//        }

            String action = intent.getAction();
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                //sb = new StringBuilder();
                List<ScanResult> wifiList = wifiManager.getScanResults();
                ArrayList<String> deviceList = new ArrayList<>();

                String GUID = getGUID();
                String fileSuffix = String.valueOf(getFileCounter());
                String fileName = "test";
                File exampleFile = new File(context.getApplicationContext().getFilesDir(), "test" + fileSuffix);
                String fileContent = "";
                try {
                    FileWriter writer = new FileWriter(exampleFile, true);
                    fileContent = "SSID,Capabilities,BSSID,Level,Timestamp,Venue Name";
                    writer.write(fileContent);
                } catch (Exception exception) {
                    Log.e("WifiReceiver", "BufferedWriter error", exception);
                }
                Log.i("WifiReceiver", "before loop of writing data");
                try {
                    FileWriter writer = new FileWriter(exampleFile, true);
                    for (ScanResult scanResult : wifiList) {
                        long microseconds = scanResult.timestamp;
                        long days = TimeUnit.MICROSECONDS.toDays(microseconds);

                        writer.write(System.getProperty("line.separator"));
                        fileContent = scanResult.SSID + "," + scanResult.capabilities + "," + scanResult.BSSID + "," + scanResult.level + "," + days + " days," + scanResult.venueName;
                        writer.write(fileContent);

                        deviceList.add("                      " + scanResult.SSID
                                + "\nCapabilities: " + scanResult.capabilities
                                + "\nBSSID: " + scanResult.BSSID
                                + "\nLevel: " + scanResult.level
                                + "\nTimestamp: " + days + " days"
                                + "\nVenue Name: " + scanResult.venueName// This was deprecated in API 31
                                + "\nPasspoint friendly name: " + scanResult.operatorFriendlyName //newer name=getPasspointProviderFriendlyName
                                + "\nIs it passpoint? " + scanResult.isPasspointNetwork());
                    }
                    writer.close();
                } catch (Exception exception) {
                    Log.e("WifiReceiver", "BufferedWriter error", exception);
                }

                StorageUploadFileOptions options = StorageUploadFileOptions.builder()
                        .accessLevel(StorageAccessLevel.PRIVATE)
                        .build();
                Amplify.Storage.uploadFile(
                        GUID + "/" + exampleFile.getName() + ".csv",
                        exampleFile,
                        options,
                        result -> Log.i("MyAmplifyApp", "Successfully uploaded: " + result.getKey()),
                        storageFailure -> Log.e("MyAmplifyApp", "Upload failed", storageFailure)
                );

                //Toast.makeText(context, sb, Toast.LENGTH_SHORT).show();
                ArrayAdapter arrayAdapter = new ArrayAdapter(context, android.R.layout.simple_list_item_1, deviceList.toArray());
                wifiDeviceList.setAdapter(arrayAdapter);
            }
        }
    }
}