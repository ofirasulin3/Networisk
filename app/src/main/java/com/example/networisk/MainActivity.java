package com.example.networisk;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    private final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 1;
    private final int WiFiPanel = 1;
    private final int LocationPanel = 2;
    WifiReceiver receiverWifi;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wifiList = (ListView) findViewById(R.id.wifiList);
        Button buttonScan = (Button) findViewById(R.id.scanBtn);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_ACCESS_FINE_LOCATION);
        }
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Permission should be granted at this point
                // Checking if Wifi & Location are Enabled
                if (!wifiManager.isWifiEnabled()) {
                    Intent panelIntentWifi = new Intent(Settings.Panel.ACTION_WIFI);
                    startActivityForResult(panelIntentWifi, WiFiPanel);
                    Toast.makeText(getApplicationContext(), "Please turn ON Wi-Fi", Toast.LENGTH_LONG).show();
                }
                else if (!isLocationEnabled()){
                    turnOnLocation();
                }
                else{
                    Toast.makeText(MainActivity.this, "scanning", Toast.LENGTH_SHORT).show();
                    wifiManager.startScan();
                }
            }
        });
    }
    //What do on when a screen closes
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(wifiManager.isWifiEnabled()) {
            if (requestCode == WiFiPanel) {//returned from wifi panel
                if (!isLocationEnabled()) {
                    turnOnLocation();
                } else {
                    Toast.makeText(MainActivity.this, "scanning", Toast.LENGTH_SHORT).show();
                    wifiManager.startScan();
                }
            } else if (requestCode == LocationPanel) {//returned from Location panel
                if (isLocationEnabled()) {
                    Toast.makeText(MainActivity.this, "scanning", Toast.LENGTH_SHORT).show();
                    wifiManager.startScan();
                } else {//Location is not enabled
                    Toast.makeText(getApplicationContext(), "Location is needed for the scan", Toast.LENGTH_LONG).show();
                }
            }
        }
        else {//wifi is not enabled
            Toast.makeText(getApplicationContext(), "Wi-Fi is needed for the scan", Toast.LENGTH_LONG).show();
        }
    }

    //What do on when returning to app
    @Override
    protected void onPostResume() {
        super.onPostResume();
        receiverWifi = new WifiReceiver(wifiManager, wifiList);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(receiverWifi, intentFilter);
        //();
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
            case MY_PERMISSIONS_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "permission granted", Toast.LENGTH_SHORT).show();
                    //don't start scan because 2 permissions are needed! //wifiManager.startScan();
                    //Toast.makeText(MainActivity.this, "starting scan", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "permission not granted", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
        }
    }
    private void turnOnLocation() {
        Intent panelIntentLocation = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(panelIntentLocation, LocationPanel);
        Toast.makeText(getApplicationContext(), "Please turn ON Location", Toast.LENGTH_LONG).show();
    }
    private boolean isLocationEnabled() {
        LocationManager lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            throw ex;
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            throw ex;
        }
        if (!gps_enabled && !network_enabled) {
            return false;
        }
        return true;

    }

}


//    private void getWifi() {
////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            //Toast.makeText(MainActivity.this, "version> = marshmallow", Toast.LENGTH_SHORT).show();
//            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(MainActivity.this, "location permission is off", Toast.LENGTH_SHORT).show();
//                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_ACCESS_COARSE_LOCATION);
//            } else if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(MainActivity.this, "fine location permission is off", Toast.LENGTH_SHORT).show();
//                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_ACCESS_FINE_LOCATION);
//            } else {
//                Toast.makeText(MainActivity.this, "All location permissions are on", Toast.LENGTH_SHORT).show();
//                //don't start scan. button is enough//wifiManager.startScan();
//                //Toast.makeText(MainActivity.this, "scanning", Toast.LENGTH_SHORT).show();
//            }
////        } else { //for older versions
////            Toast.makeText(MainActivity.this, "All location permissions are on", Toast.LENGTH_SHORT).show();
////            wifiManager.startScan();
////            Toast.makeText(MainActivity.this, "scanning2", Toast.LENGTH_SHORT).show();
////        }
//    }

