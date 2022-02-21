package com.example.networisk;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.auth.AuthUserAttributeKey;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.auth.options.AuthSignUpOptions;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.storage.StorageAccessLevel;
import com.amplifyframework.storage.options.StorageUploadFileOptions;
import com.amplifyframework.storage.s3.AWSS3StoragePlugin;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private ListView wifiList;
    private WifiManager wifiManager;
    private final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 1;
    private final int MY_PERMISSIONS_ACCESS_BACKGROUND_LOCATION = 2;
    private final int WiFiPanel = 1;
    private final int LocationPanel = 2;
    WifiReceiver receiverWifi;
    LocationManager locationManager;
    private Location focalLocation;
    private Location currentLocation;
    LocationListener locationListener = new MyLocationListener();
    private GeofencingClient geofencingClient;
    ArrayList<Geofence> geofenceList = new ArrayList<Geofence>();
    PendingIntent geofencePendingIntent;
    private static int inside = 0;
    private SharedPreferences sharedPref;

    public static void setInside(int val) {
        inside = val;
    }

    public static int getInside() {
        return inside;
    }

//    public String getGUID() {
//        String GUID = sharedPref.getString("guid", "-1");
//        if(GUID.equals("-1")) {
//            SharedPreferences.Editor editor = sharedPref.edit();
//            GUID = UUID.randomUUID().toString();
//            editor.putString("guid", GUID);
//            editor.apply();
//        }
//        return GUID;
//    }
//
//    public int getFileCounter() {
//        int fileCounter = sharedPref.getInt("fileCounter", 0) + 1;
//        SharedPreferences.Editor editor = sharedPref.edit();
//        editor.putInt("fileCounter", fileCounter);
//        editor.apply();
//        return fileCounter;
//    }

//    public void uploadFile() {
//        File exampleFile = new File(getApplicationContext().getFilesDir(), "testFile"+ WifiReceiver.getFileCounter());
////        Writing to the file
////        try {
////            BufferedWriter writer = new BufferedWriter(new FileWriter(exampleFile));
////            writer.append("Example file contents");
////            writer.close();
////        } catch (Exception exception) {
////            Log.e("MyAmplifyApp", "Upload failed", exception);
////        }
//
//        String GUID = getGUID();
//        String fileSuffix = String.valueOf(getFileCounter());
//        String fileName = "test";
//
//        StorageUploadFileOptions options = StorageUploadFileOptions.builder()
//                .accessLevel(StorageAccessLevel.PRIVATE)
//                .build();
//        Amplify.Storage.uploadFile(
//                GUID + "/" + exampleFile.getName() + ".csv",
//                exampleFile,
//                options,
//                result -> Log.i("MyAmplifyApp", "Successfully uploaded: " + result.getKey()),
//                storageFailure -> Log.e("MyAmplifyApp", "Upload failed", storageFailure)
//        );
//    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {

            /*Note that because the storage category requires auth,
             you will need to either configure guest access
             https://docs.amplify.aws/lib/auth/guest_access/q/platform/android/
             or sign in a user before using features in the storage category.
             https://docs.amplify.aws/lib/auth/signin/q/platform/android/ */

            // Add these lines to add the AWSCognitoAuthPlugin and AWSS3StoragePlugin plugins
            Amplify.addPlugin(new AWSCognitoAuthPlugin());
            Amplify.addPlugin(new AWSS3StoragePlugin());

            Amplify.configure(getApplicationContext());
            Log.i("MyAmplifyApp", "Initialized Amplify");

            Amplify.Auth.fetchAuthSession(
                    result -> Log.i("AmplifyQuickstart", result.toString()),
                    error -> Log.e("AmplifyQuickstart", error.toString())
            );

            /*AuthSignUpOptions options = AuthSignUpOptions.builder()
                    .userAttribute(AuthUserAttributeKey.email(), "ofirasulin3@gmail.com")
                    .build();
            Amplify.Auth.signUp("networisk", "networisk123", options,
                    result -> Log.i("AuthQuickStart", "Result: " + result.toString()),
                    error -> Log.e("AuthQuickStart", "Sign up failed", error)
                    //I/AuthQuickStart: Result: AuthSignUpResult{isSignUpComplete=true, nextStep=AuthNextSignUpStep{signUpStep=CONFIRM_SIGN_UP_STEP, additionalInfo={}, codeDeliveryDetails=AuthCodeDeliveryDetails{destination='m***@e***.com', deliveryMedium=EMAIL, attributeName='email'}}, user=AuthUser{userId='81136262-b6d8-448d-a7b1-2148eb65507a', username='username'}}
            );*/

            /*Amplify.Auth.confirmSignUp(
                    "networisk",
                    "966136", //826808 ofir
                    result -> Log.i("AuthQuickstart", result.isSignUpComplete() ? "Confirm signUp succeeded" : "Confirm sign up not complete"),
                    error -> Log.e("AuthQuickstart", error.toString())
            );*/

            Amplify.Auth.signIn(
                    "networisk", //ofir
                    "networisk123",
                    result -> Log.i("AuthQuickstart", result.isSignInComplete() ? "Sign in succeeded" : "Sign in not complete"),
                    error -> Log.e("AuthQuickstart", error.toString())
            );

//            uploadFile();

        } catch (AmplifyException error) {
            Log.e("MyAmplifyApp", "Could not initialize Amplify", error);
        }
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        setContentView(R.layout.activity_main);
        wifiList = (ListView) findViewById(R.id.wifiList);
        Button ScanBtn = (Button) findViewById(R.id.scanBtn);
        Button LocationBtn = (Button) findViewById(R.id.locationBtn);


        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_ACCESS_FINE_LOCATION);
        }

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, MY_PERMISSIONS_ACCESS_BACKGROUND_LOCATION);
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

        geofencingClient = LocationServices.getGeofencingClient(this);

        geofenceList.add(new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId("GeoFence1")

                .setCircularRegion(
                        32.777804,
                        35.021855,
                        150 //the optimal minimum radius of the geofence should be set between 100 - 150 meter
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());

        geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences added
                        Log.i("Geofence", "Geofences added successfully");
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to add geofences
                        Log.e("Geofence", "Failed to add geofences", e);
                    }
                });

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

                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, MY_PERMISSIONS_ACCESS_BACKGROUND_LOCATION);
                    }


                    Location last_loc = getLastKnownLocationAux();
                    if(last_loc!=null) {
                        focalLocation = last_loc;
                        geofenceList = new ArrayList<Geofence>();
                        geofenceList.add(new Geofence.Builder()
                                // Set the request ID of the geofence. This is a string to identify this
                                // geofence.
                                .setRequestId("GeoFence1")

                                .setCircularRegion(
                                        focalLocation.getLatitude(),
                                        focalLocation.getLongitude(),
                                        150 //the optimal minimum radius of the geofence should be set between 100 - 150 meter
                                )
                                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                                        Geofence.GEOFENCE_TRANSITION_EXIT)
                                .build());

                        receiverWifi = new WifiReceiver(wifiManager, wifiList, sharedPref);
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
                    //if(focalLocation.distanceTo(currentLocation)<=200) {
                    Toast.makeText(MainActivity.this, "scanning", Toast.LENGTH_SHORT).show();
                    wifiManager.startScan();
                    //}
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
        receiverWifi = new WifiReceiver(wifiManager, wifiList,sharedPref);
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
