package com.example.networisk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    private final int ENTERED = 1;
    private final int EXITED = 0;

    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceStatusCodes
                    .getStatusCodeString(geofencingEvent.getErrorCode());
            Log.e("GeofenceBroadcastReceiver onReceive", errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                MainActivity.inside = ENTERED;
                Log.i("ENTERED", "ENTERED");
            }
            else {
                MainActivity.inside = EXITED;
                Log.i("EXITED", "EXITED");
            }
            Log.i("GeofenceBroadcastReceiver onReceive", String.valueOf(geofenceTransition) + " " + String.valueOf(triggeringGeofences));
        } else {
            // Log the error.
            Log.e("GeofenceBroadcastReceiver onReceive", "geofence_transition_invalid_type " + String.valueOf(geofenceTransition));
        }
    }
}