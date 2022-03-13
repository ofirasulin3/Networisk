package com.example.networisk;

import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Calendar;
import java.util.Date;

public class UploadWorker extends Worker {
    private WifiManager WorkerWifiManager;
    private WifiReceiver WorkerReceiverWifi = null;

    public UploadWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);

        this.WorkerWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    public Result doWork() {
        // Do the work here--in this case, upload the files.
        startScanWorker();
        // Indicate whether the work finished successfully with the Result
        return Result.success();
    }

    private void startScanWorker() {
        Date currentTime = Calendar.getInstance().getTime();
        Log.i("UploadWorker", "working: "+currentTime.toString());
        if (WorkerReceiverWifi != null) {
            getApplicationContext().unregisterReceiver(WorkerReceiverWifi);
        }
        WifiReceiver WorkerReceiverWifi = new WifiReceiver(WorkerWifiManager, getApplicationContext().getSharedPreferences("sharedPref",0));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getApplicationContext().registerReceiver(WorkerReceiverWifi, intentFilter);
        WorkerWifiManager.startScan();
    }



}
