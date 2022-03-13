package com.example.networisk;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

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

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private ListView wifiList;
    private SharedPreferences sharedPref;
    private WifiManager wifiManager;
    private double TaubLatitude = 32.777804;
    private double TaubLongitude = 35.021855;
    private Location currentLocation;
    private Location focalLocation;
    public static int inside = 0;
    private final int WiFiPanelRequestCode = 1;
    private final int LocationPanelRequestCode = 2;
    private boolean AmplifySignUp = true;
    private final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 1;
    private final int MY_PERMISSIONS_ACCESS_BACKGROUND_LOCATION = 2;
    private GeofencingClient geofencingClient;
    ArrayList<Geofence> geofenceList = new ArrayList<Geofence>();
    PendingIntent geofencePendingIntent;
    LocationManager locationManager;
    LocationListener locationListener = new MyLocationListener();
    Button ScanBtn;
    Button LocationBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        wifiList = (ListView) findViewById(R.id.wifiList);
        ScanBtn = (Button) findViewById(R.id.scanBtn);
        LocationBtn = (Button) findViewById(R.id.locationBtn);

        try {
            amplifyOnCreate();
        } catch (AmplifyException error) {
            Log.e("MyAmplifyApp", "Could not initialize Amplify", error);
        }

        sharedPref = getApplicationContext().getSharedPreferences("sharedPref",MODE_PRIVATE);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if ((ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            onCreateAux();
        } else {
            LocationBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requestAllPermissions();
                }
            });
            ScanBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requestAllPermissions();
                }
            });
            requestAllPermissions(); // ACCESS_FINE_LOCATION, ACCESS_BACKGROUND_LOCATION
        }
    }

    private void onCreateAux() {
        setFirstLocation(); // Taub Latitude & Longitude

        geofenceCreateAdd(TaubLatitude, TaubLongitude, "Taub");

        currentLocation = getLastKnownLocationAux();

        if (!isLocationEnabled()) {
            turnOnLocation();
        }

        LocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isLocationEnabled()) {
                    turnOnLocation();
                } else {
                    Location last_loc = getLastKnownLocationAux();
                    if(last_loc != null) {
                        focalLocation = last_loc;
                        geofenceRemove();
                        inside = 0; // Outside from the geofence by default
                        geofenceCreateAdd(focalLocation.getLatitude(),focalLocation.getLongitude(),"myLoc");

                        setLocationInSharedPref(focalLocation.getLatitude(),focalLocation.getLongitude());
                        Toast.makeText(MainActivity.this,
                                "New Focal Location: \n" +
                                        "Lat- " + focalLocation.getLatitude() + "\n" +
                                        "Lng- " + focalLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        if (!wifiManager.isWifiEnabled()) {
            turnOnWIFI();
        }

        ScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!wifiManager.isWifiEnabled()) {
                    turnOnWIFI();
                } else {
                    startWorker();
                }
            }
        });
    }

    private void startWorker() {
        PeriodicWorkRequest myUploadWork = new PeriodicWorkRequest.Builder(UploadWorker.class, 15, TimeUnit.MINUTES).build();
        WorkManager
            .getInstance(MainActivity.this)
            .enqueueUniquePeriodicWork(
                "UploadWork",
                ExistingPeriodicWorkPolicy.REPLACE,
                myUploadWork
            );
    }

    @Override //What do when a screen closes
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LocationPanelRequestCode && !isLocationEnabled()) {
            Toast.makeText(getApplicationContext(), "Location is needed for the scan", Toast.LENGTH_LONG).show();
        }
        if (requestCode == WiFiPanelRequestCode && !wifiManager.isWifiEnabled()) {
            Toast.makeText(getApplicationContext(), "Wi-Fi is needed for the scan", Toast.LENGTH_LONG).show();
        }
    }

    @Override //What do when returning to app
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "FINE_LOCATION permission granted", Toast.LENGTH_SHORT).show();
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, MY_PERMISSIONS_ACCESS_BACKGROUND_LOCATION);
                }
            } else {
                Toast.makeText(MainActivity.this, "FINE_LOCATION permission not granted", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (requestCode == MY_PERMISSIONS_ACCESS_BACKGROUND_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "BACKGROUND_LOCATION permission granted", Toast.LENGTH_SHORT).show();
                onCreateAux();
            } else {
                Toast.makeText(MainActivity.this, "BACKGROUND_LOCATION permission not granted", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    private void amplifyOnCreate() throws AmplifyException {
        String mail = "networisk@gmail.com";
        String name = "networisk";
        String password = "networisk123";

        Amplify.addPlugin(new AWSCognitoAuthPlugin());
        Amplify.addPlugin(new AWSS3StoragePlugin());
        Amplify.configure(getApplicationContext());
        Log.i("MyAmplifyApp", "Initialized Amplify");
        Amplify.Auth.fetchAuthSession(
                result -> Log.i("AmplifyQuickstart", result.toString()),
                error -> Log.e("AmplifyQuickstart", error.toString())
        );

        if (AmplifySignUp) {
            AuthSignUpOptions options = AuthSignUpOptions.builder()
                    .userAttribute(AuthUserAttributeKey.email(), mail).build();
            Amplify.Auth.signUp(name, password, options,
                    result -> Log.i("AmplifyAuthQuickStart", "Result: " + result.toString()),
                    error -> Log.e("AmplifyAuthQuickStart", "Sign up failed", error)
            );
            AmplifySignUp = false;
        }
        Amplify.Auth.signIn(
                name,
                password,
                result -> Log.i("AmplifyAuthQuickstart", result.isSignInComplete() ? "Sign in succeeded" : "Sign in not complete"),
                error -> Log.e("AmplifyAuthQuickstart", error.toString())
        );
    }

    private void requestAllPermissions() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_ACCESS_FINE_LOCATION);
        }
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, MY_PERMISSIONS_ACCESS_BACKGROUND_LOCATION);
        }
    }

    @SuppressLint("MissingPermission")
    private void setFirstLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,8000,7, locationListener);
        focalLocation = new Location("First Focal Location");

        double TaubLatitude = 32.777804;
        double TaubLongitude = 35.021855;
        focalLocation.setLatitude(TaubLatitude);
        focalLocation.setLongitude(TaubLongitude);
        setLocationInSharedPref(TaubLatitude,TaubLongitude);
    }

    @SuppressLint("MissingPermission")
    private void geofenceCreateAdd(double Lat, double Lon, String id) {
        geofencingClient = LocationServices.getGeofencingClient(this);
        geofenceList.add(new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this geofence.
                .setRequestId(id)
                .setCircularRegion(
                        Lat,
                        Lon,
                        450 //the optimal minimum radius of the geofence should be set between 100 - 150 meter
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT).build());

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
    }

    public void geofenceRemove() {
        geofencingClient.removeGeofences(getGeofencePendingIntent())
            .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // Geofences removed
                    Log.i("Geofence", "Geofences removed successfully");
                }
            })
            .addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Failed to remove geofences
                    Log.e("Geofence", "Failed to remove geofences", e);
                }
            });
    }

    @SuppressLint("MissingPermission")
    private Location getLastKnownLocationAux() {
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        int i = 1;
        for (String provider : providers) {
            Location loc = locationManager.getLastKnownLocation(provider);

            if (loc == null) continue;

            if (bestLocation == null || loc.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = loc;
            }
        }
        if (bestLocation == null) return null;
        return bestLocation;
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

    private void turnOnLocation() {
        Intent panelIntentLocation = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(panelIntentLocation, LocationPanelRequestCode);
        Toast.makeText(getApplicationContext(), "Please turn ON Location", Toast.LENGTH_LONG).show();
    }

    private void setLocationInSharedPref(double Lat, double Lon) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong("Lat", Double.doubleToLongBits(Lat));
        editor.putLong("Lon", Double.doubleToLongBits(Lon));
        editor.apply();
    }

    private void turnOnWIFI() {
        Intent panelIntentWifi = new Intent(Settings.Panel.ACTION_WIFI);
        startActivityForResult(panelIntentWifi, WiFiPanelRequestCode);
        Toast.makeText(getApplicationContext(), "Please turn ON Wi-Fi", Toast.LENGTH_LONG).show();
    }

    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
            currentLocation = loc;
        }
        @Override public void onProviderDisabled(String provider) {}
        @Override public void onProviderEnabled(String provider) {}
        @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
    }

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



    public void f() {}

}