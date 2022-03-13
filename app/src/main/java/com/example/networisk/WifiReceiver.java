package com.example.networisk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.amplifyframework.core.Amplify;
import com.amplifyframework.storage.StorageAccessLevel;
import com.amplifyframework.storage.options.StorageUploadFileOptions;

import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class WifiReceiver extends BroadcastReceiver {
    private SharedPreferences sharedPref;
    private WifiManager wifiManager;
    private Calendar now;
    private long start;
    private int counter = 0;

    public WifiReceiver(WifiManager wifiManager, SharedPreferences sharedPref) {
        this.wifiManager = wifiManager;
        this.sharedPref = sharedPref;
        long start = System.nanoTime();
    }

    public String getGUID() {
//        String GUID = sharedPref.getString("guid", "-1");
//        if(GUID.equals("-1")) {
//            SharedPreferences.Editor editor = sharedPref.edit();
//            GUID = UUID.randomUUID().toString();
//            editor.putString("guid", GUID);
//            editor.apply();
//        }
//        return GUID;
        return "newTest612";
    }

    public int getFileCounter() {
        int fileCounter = sharedPref.getInt("fileCounter", 0) + 1;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("fileCounter", fileCounter);
        editor.apply();
        return fileCounter;
    }

    public void onReceive(Context context, Intent intent) {
        long end = System.nanoTime();
        long elapsedTime = end - start;
        start = end;
        if (elapsedTime * Math.pow(10, -9) >= 60 || counter == 0) {
            if (MainActivity.inside == 0) {
                Log.i("WifiReceiver", "Outside Geofence");
                return;
            } else {
                Log.i("WifiReceiver", "Inside Geofence");

                String action = intent.getAction();
                if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                    List<ScanResult> wifiList = wifiManager.getScanResults();

                    String GUID = getGUID();
                    String fileSuffix = String.valueOf(getFileCounter());
                    String fileName = "test";
                    File exampleFile = new File(context.getApplicationContext().getFilesDir(), "test" + fileSuffix);
                    String fileContent = "";
                    try {
                        FileWriter writer = new FileWriter(exampleFile, true);
                        fileContent = "SSID,Capabilities,BSSID,Level,Timestamp,Venue Name,ScanTime";
                        writer.write(fileContent);

                        String ScanTime = Calendar.getInstance().getTime().toString();

                        for (ScanResult scanResult : wifiList) {
                            long microseconds = scanResult.timestamp;
                            long days = TimeUnit.MICROSECONDS.toDays(microseconds);

                            writer.write(System.getProperty("line.separator"));
                            fileContent = scanResult.SSID + "," + scanResult.capabilities + "," + scanResult.BSSID + "," + scanResult.level + "," + days + " days," + scanResult.venueName + "," + ScanTime;
                            writer.write(fileContent);
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
                    //context.unregisterReceiver(this);
                }
            }
        }
        counter++;
    }
}
