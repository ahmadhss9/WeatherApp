package com.example.weatherapp.utils;

// Steps 41-46: location helper — permission, coordinates, city name.

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;                    // Step 46
import android.location.Geocoder;                   // Step 46
import android.location.Location;                  // Step 44
import android.os.Handler;                         // Step 45
import android.os.Looper;                          // Step 44
import android.util.Log;                           // Step 46

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;   // Step 44
import com.google.android.gms.location.LocationRequest;    // Step 44
import com.google.android.gms.location.LocationResult;     // Step 44
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;           // Step 44

import java.io.IOException;                        // Step 46
import java.util.List;                             // Step 46
import java.util.Locale;                           // Step 46

/** Steps 41-46: everything about the device location in one place. */
public class LocationHelper {

    private static final String TAG = "LOCATION";                  // Step 46

    /** Step 41: how the coordinates come back. */
    public interface Callback {
        void onLocation(double lat, double lon);
        void onError(String reason);
    }

    /** Step 46: how the city name comes back (null = unresolved). */
    public interface CityCallback {
        void onCity(String cityName);
    }

    private final Context context;                     // Step 41: app context, no Activity leak
    private final FusedLocationProviderClient client;  // Step 41: Google's location API

    public LocationHelper(Context context) {
        this.context = context.getApplicationContext();          // Step 41
        this.client = LocationServices.getFusedLocationProviderClient(this.context);   // Step 41
    }

    /** Step 42: is the app allowed to read the location? */
    public boolean hasPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /** Step 44: last known location, or one fresh fix if we have none. */
    @SuppressLint("MissingPermission")                 // we check hasPermission() first
    public void requestLocation(Callback callback) {

        if (!hasPermission()) {                        // Step 44
            callback.onError("no permission");
            return;
        }

        client.getLastLocation()                       // Step 44: cached, instant, may be null
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        callback.onLocation(location.getLatitude(), location.getLongitude());
                    } else {
                        requestFreshFix(callback);     // Step 44: no cached fix
                    }
                })
                .addOnFailureListener(e -> requestFreshFix(callback));
    }

    /** Step 44: one balanced-accuracy fix, then stop listening. */
    @SuppressLint("MissingPermission")
    private void requestFreshFix(Callback callback) {

        LocationRequest request = new LocationRequest.Builder(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY, 10_000L)   // Step 44: 10 s
                .setMaxUpdates(1)                                       // Step 44: one shot
                .build();

        LocationCallback listener = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                client.removeLocationUpdates(this);                   // Step 44: stop listening
                Location loc = result.getLastLocation();
                if (loc != null) {
                    callback.onLocation(loc.getLatitude(), loc.getLongitude());
                } else {
                    callback.onError("no fix returned");              // Step 45
                }
            }
        };

        client.requestLocationUpdates(request, listener, Looper.getMainLooper())
                .addOnFailureListener(e -> callback.onError("request failed: " + e.getMessage()));
    }

    /** Step 46: coordinates -> "City, Country" (blocking, so off the main thread). */
    @SuppressWarnings("deprecation")
    public void resolveCityName(double lat, double lon, CityCallback callback) {

        if (!Geocoder.isPresent()) {                   // Step 46: service missing on some devices
            Log.w(TAG, "Geocoder not available on this device");
            callback.onCity(null);
            return;
        }

        new Thread(() -> {                             // Step 46: never block the UI thread

            String result = null;

            try {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);   // blocking

                if (addresses != null && !addresses.isEmpty()) {
                    Address a = addresses.get(0);

                    String city = a.getLocality();         // Step 46: "Islamabad"
                    if (city == null) city = a.getSubAdminArea();
                    if (city == null) city = a.getAdminArea();

                    String country = a.getCountryName();

                    if (city != null) {
                        result = (country != null) ? city + ", " + country : city;
                    }
                }
            } catch (IOException | IllegalArgumentException e) {
                Log.w(TAG, "Reverse geocoding failed: " + e.getMessage());
            }

            String finalResult = result;
            new Handler(Looper.getMainLooper()).post(() -> callback.onCity(finalResult));   // Step 46: back to UI thread

        }).start();
    }
}