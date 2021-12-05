package com.example.networisk;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.os.Bundle;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import java.util.List;
//import android.widget.Button;

public class MainActivity extends AppCompatActivity {
//    public Context context = getApplicationContext();
    private static Context context;
    private static WifiManager wifiManager;
    private List<ScanResult> results;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("print", "onCreate");
//        System.out.printf("onCreate onCreate onCreate onCreate!");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainActivity.context = getApplicationContext();
        MainActivity.wifiManager =
                (WifiManager) MainActivity.context.getSystemService(Context.WIFI_SERVICE);
    }

    public void Initialize(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(wifiScanReceiver, intentFilter);
//        context.registerReceiver(wifiScanReceiver, intentFilter);

        boolean success = wifiManager.startScan();
        Log.i("print", "started scan");
//        if (!success) {
//            // scan failure handling
//            Log.i("print", "scanFailure2");
//            scanFailure();
//        }
    }

    BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            //TODO: turn on wifi
             results = wifiManager.getScanResults();
             unregisterReceiver(this);
             for(ScanResult result:results)
                 Log.i("print", (String) result.venueName);
             Log.i("print", "finished for loop");

//            boolean success = intent.getBooleanExtra(
//                    WifiManager.EXTRA_RESULTS_UPDATED, false);
//            if (success) {
//                scanSuccess();
//            } else {
//                // scan failure handling
//                Log.i("print", "scanFailure1");
//                scanFailure();
//            }
        }
    };

    private void scanSuccess(){
        Log.i("print", "scanSuccess!");
        List<ScanResult> results = wifiManager.getScanResults();
        // use new scan results ...
        for(ScanResult result:results)
            Log.i("print", (String) result.venueName);
        Log.i("print", "finished_scanSuccess");

    }

    private void scanFailure(){
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        Log.i("print", "scanFailure!");
        List<ScanResult> results = wifiManager.getScanResults();
        for(ScanResult result:results)
            Log.i("print", (String) result.venueName);
        Log.i("print", "finished_scanFailure");
        // potentially use older scan results ...
    }

    public void scanClicked(View view) {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        } else {
            Initialize();
//            wifiManager.startScan();
        }
    }

    public void switchWIFI(Boolean isON) {
        // if it is Android Q and above go for the newer approach
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
            startActivityForResult(panelIntent, 1);
        } else {
            WifiManager wifi_mngr =
                    (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifi_mngr.setWifiEnabled(isON);
        }
    }

    public void switchWifiOn(View view) {
        switchWIFI(true);
    }
    public void switchWifiOff(View view) {
        switchWIFI(false);
    }

}