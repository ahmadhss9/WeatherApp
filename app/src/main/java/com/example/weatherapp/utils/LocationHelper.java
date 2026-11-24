package com.example.weatherapp.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

/** Step 41-45: everything about finding the device location in one place. */
public class LocationHelper {

    public interface Callback {                 // Step 41: how we deliver the answer
        void onLocation(double lat, double lon);
        void onError(String reason);
    }

    private final Context context;
    private final FusedLocationProviderClient client;

    public LocationHelper(Context context) {
        this.context = context.getApplicationContext();          // avoid holding the Activity
        this.client = LocationServices.getFusedLocationProviderClient(this.context);
    }

    /** Step 42: is the app allowed to read the location? */
    public boolean hasPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /** Step 44: will fetch real coordinates (placeholder for now). */
    public void requestLocation(Callback callback) {
        callback.onError("requestLocation not implemented yet");
    }
}