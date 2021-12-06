package com.example.networisk;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private ListView wifiList;
    private WifiManager wifiManager;
    private final int MY_PERMISSIONS_ACCESS_COARSE_LOCATION = 1;
    private final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 1;
    WifiReceiver receiverWifi;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wifiList = (ListView) findViewById(R.id.wifiList);
        Button buttonScan = (Button) findViewById(R.id.scanBtn);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(getApplicationContext(), "Turning WiFi ON...", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_ACCESS_COARSE_LOCATION);
                }
                else if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_ACCESS_FINE_LOCATION);

                } else {
                    wifiManager.startScan();
                    Toast.makeText(MainActivity.this, "scanning (button)", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    protected void onPostResume() {
        super.onPostResume();
        receiverWifi = new WifiReceiver(wifiManager, wifiList);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(receiverWifi, intentFilter);
        getWifi();
    }
    private void getWifi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Toast.makeText(MainActivity.this, "version> = marshmallow", Toast.LENGTH_SHORT).show();
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "location turned off", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_ACCESS_COARSE_LOCATION);
            } else if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "fine location turned off", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_ACCESS_FINE_LOCATION);
            } else {
                Toast.makeText(MainActivity.this, "location turned on", Toast.LENGTH_SHORT).show();
                wifiManager.startScan();
                Toast.makeText(MainActivity.this, "scanning", Toast.LENGTH_SHORT).show();
            }
        } else {
            wifiManager.startScan();
            Toast.makeText(MainActivity.this, "scanning2", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiverWifi);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "permission granted", Toast.LENGTH_SHORT).show();
                    wifiManager.startScan();
                    Toast.makeText(MainActivity.this, "scanning4", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "permission not granted", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
        }
    }
}

//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//
//import android.Manifest;
//import android.content.BroadcastReceiver;
//import android.content.IntentFilter;
//import android.content.pm.PackageManager;
//import android.net.wifi.ScanResult;
//import android.os.Bundle;
//
//import android.content.Context;
//import android.content.Intent;
//import android.net.wifi.WifiManager;
//import android.os.Build;
//import android.provider.Settings;
//import android.util.Log;
//import android.view.View;
//
//import java.util.List;
////import android.widget.Button;
//
//public class MainActivity extends AppCompatActivity {
////    public Context context = getApplicationContext();
//    private static Context context;
//    private static WifiManager wifiManager;
//    private List<ScanResult> results;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        Log.i("print", "onCreate");
////        System.out.printf("onCreate onCreate onCreate onCreate!");
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        MainActivity.context = getApplicationContext();
//        MainActivity.wifiManager =
//                (WifiManager) MainActivity.context.getSystemService(Context.WIFI_SERVICE);
//    }
//
//    public void Initialize(){
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
//        registerReceiver(wifiScanReceiver, intentFilter);
////        context.registerReceiver(wifiScanReceiver, intentFilter);
//
//        boolean success = wifiManager.startScan();
//        Log.i("print", "started scan");
////        if (!success) {
////            // scan failure handling
////            Log.i("print", "scanFailure2");
////            scanFailure();
////        }
//    }
//
//    BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context c, Intent intent) {
//            //need to turn on wifi first
//             results = wifiManager.getScanResults();
//             unregisterReceiver(this);
//             for(ScanResult result:results)
//                 Log.i("print", (String) result.venueName);
//             Log.i("print", "finished for loop");
//
////            boolean success = intent.getBooleanExtra(
////                    WifiManager.EXTRA_RESULTS_UPDATED, false);
////            if (success) {
////                scanSuccess();
////            } else {
////                // scan failure handling
////                Log.i("print", "scanFailure1");
////                scanFailure();
////            }
//        }
//    };
//
//    private void scanSuccess(){
//        Log.i("print", "scanSuccess!");
//        List<ScanResult> results = wifiManager.getScanResults();
//        // use new scan results ...
//        for(ScanResult result:results)
//            Log.i("print", (String) result.venueName);
//        Log.i("print", "finished_scanSuccess");
//
//    }
//
//    private void scanFailure(){
//        // handle failure: new scan did NOT succeed
//        // consider using old scan results: these are the OLD results!
//        Log.i("print", "scanFailure!");
//        List<ScanResult> results = wifiManager.getScanResults();
//        for(ScanResult result:results)
//            Log.i("print", (String) result.venueName);
//        Log.i("print", "finished_scanFailure");
//        // potentially use older scan results ...
//    }
//
//    public void scanClicked(View view) {
//        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
//        != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(
//                    MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
//        } else {
//            Initialize();
////            wifiManager.startScan();
//        }
//    }
//
//    public void switchWIFI(Boolean isON) {
//        // if it is Android Q and above go for the newer approach
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
//            startActivityForResult(panelIntent, 1);
//        } else {
//            WifiManager wifi_mngr =
//                    (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//            wifi_mngr.setWifiEnabled(isON);
//        }
//    }
//
//    public void switchWifiOn(View view) {
//        switchWIFI(true);
//    }
//    public void switchWifiOff(View view) {
//        switchWIFI(false);
//    }
//
//}

//package tdk.cs.technion.ac.il.risknet;

