package com.example.networisk;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ListView wifiList;
    private WifiManager wifiManager;
    private final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 1;
    private final int WiFiPanel = 1;
    private final int LocationPanel = 2;
    WifiReceiver receiverWifi;

    LocationManager locationManager;
    private Location focalLocation;
    private Location currentLocation;
    LocationListener locationListener = new MyLocationListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wifiList = (ListView) findViewById(R.id.wifiList);
        Button ScanBtn = (Button) findViewById(R.id.scanBtn);
        Button LocationBtn = (Button) findViewById(R.id.locationBtn);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_ACCESS_FINE_LOCATION);
        }

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        //8000ms = 8sec-> minimum time for new updates
        //7m-> minimum distance for new updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,8000,7, locationListener);
        //String provider = locationManager.getBestProvider(criteria, true);
        //Location location = locationManager.getLastKnownLocation(provider);
        focalLocation = new Location("First Focal Location");
        //Taub: 32.777804, 35.021855
        focalLocation.setLatitude(32.777804);
        focalLocation.setLongitude(35.021855);
        currentLocation=getLastKnownLocationAux();

        LocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Permission should be granted at this point
                // Checking if Location is Enabled
                if (!isLocationEnabled()){
                    turnOnLocation();
                } else{
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_ACCESS_FINE_LOCATION);
                    }
                    //TODO: maybe should put here "else"
                    //Location last_loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    //Location last_loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                    Location last_loc = getLastKnownLocationAux();
                    if(last_loc!=null) {
                        focalLocation = last_loc;
                        receiverWifi = new WifiReceiver(wifiManager, wifiList, focalLocation, currentLocation);
                        IntentFilter intentFilter = new IntentFilter();
                        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
                        registerReceiver(receiverWifi, intentFilter);
                        Toast.makeText(MainActivity.this,
                                "New Focal Location: \n" +
                                        "Lat- " + focalLocation.getLatitude() + "\n" +
                                        "Lng- " + focalLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        ScanBtn.setOnClickListener(new View.OnClickListener() {
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

    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
//        editLocation.setText("");
//        pb.setVisibility(View.INVISIBLE);
            currentLocation = loc;
//            lastLatitude = loc.getLatitude();
//            lastLongitude = loc.getLongitude();
            Toast.makeText(MainActivity.this,
                       "Current Location changed.\n" +
                            "Lat- " + currentLocation.getLatitude() + "\n" +
                            "Lng- " + currentLocation.getLongitude(), Toast.LENGTH_LONG).show();
//            Log.v("Tagl", "Focal Longitude: " + longitude);
//            Log.v("Tag", "Focal Latitude: " + latitude);
//            String s = longitude + "\n" + latitude;
//            editLocation.setText(s);
        }
        @Override public void onProviderDisabled(String provider) {}
        @Override public void onProviderEnabled(String provider) {}
        @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
    }

    private Location getLastKnownLocationAux() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_ACCESS_FINE_LOCATION);
        }
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        int i = 1;
        for (String provider : providers) {
            Location loc = locationManager.getLastKnownLocation(provider);
//            ALog.d("last known location, provider: %s, location: %s", provider,
//                    l);
            if (loc == null) continue;
//            Toast.makeText(MainActivity.this,
//                        "Location Option " + i++ + ":\n" +
//                             "Lat- " + loc.getLatitude() + "\n" +
//                             "Lng- " + loc.getLongitude(), Toast.LENGTH_SHORT).show();

            if (bestLocation == null
                    || loc.getAccuracy() < bestLocation.getAccuracy()) {
//                ALog.d("found best last known location: %s", loc);
                bestLocation = loc;
            }
        }
        if (bestLocation == null) return null;
//        Toast.makeText(MainActivity.this,
//                "Last Known Location:\n" +
//                        "Lat- " + bestLocation.getLatitude() + "\n" +
//                        "Lng- " + bestLocation.getLongitude(), Toast.LENGTH_LONG).show();
        return bestLocation;
    }

    @Override //What do on when a screen closes
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
                } else { //Location is not enabled
                    Toast.makeText(getApplicationContext(), "Location is needed for the scan", Toast.LENGTH_LONG).show();
                }
            }
        }
        else {//wifi is not enabled
            Toast.makeText(getApplicationContext(), "Wi-Fi is needed for the scan", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPostResume() { //What do on when returning to app
        super.onPostResume();
        receiverWifi = new WifiReceiver(wifiManager, wifiList, focalLocation, currentLocation);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(receiverWifi, intentFilter);
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
