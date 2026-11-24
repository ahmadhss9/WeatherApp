package com.example.weatherapp.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import android.location.Address;                                 // Step 46
import android.location.Geocoder;                                // Step 46
import java.io.IOException;                                      // Step 46
import java.util.List;                                           // Step 46
import java.util.Locale;                                         // Step 46
/** Step 41-45: everything about finding the device location in one place. */
private static final String TAG = "LOCATION";                // Step 46

/** Step 46: city name answer (null when unresolved). */
public interface CityCallback {
    void onCity(String cityName);
}
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

    /** Step 44: last known location, or one fresh fix if we have none. */
    @SuppressLint("MissingPermission")        // we check hasPermission() first
    public void requestLocation(Callback callback) {

        if (!hasPermission()) {
            callback.onError("no permission");
            return;
        }

        client.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        callback.onLocation(location.getLatitude(), location.getLongitude());
                    } else {
                        requestFreshFix(callback);          // no cached fix
                    }
                })
                .addOnFailureListener(e -> requestFreshFix(callback));
    }

    /** Step 44: one high-level fix, then stop listening. */
    @SuppressLint("MissingPermission")
    private void requestFreshFix(Callback callback) {

        LocationRequest request = new LocationRequest.Builder(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY, 10_000L)   // 10 s
                .setMaxUpdates(1)
                .build();

        LocationCallback listener = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                client.removeLocationUpdates(this);                   // stop listening
                Location loc = result.getLastLocation();
                if (loc != null) {
                    callback.onLocation(loc.getLatitude(), loc.getLongitude());
                } else {
                    callback.onError("no fix returned");
                }
            }
        };
        /** Step 46: coordinates -> "City, Country" (runs off the main thread). */
        public void resolveCityName(double lat, double lon, CityCallback callback) {

            if (!Geocoder.isPresent()) {
                Log.w(TAG, "Geocoder not available on this device");
                callback.onCity(null);
                return;
            }

            new Thread(() -> {

                String result = null;

                try {
                    Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);   // blocking

                    if (addresses != null && !addresses.isEmpty()) {
                        Address a = addresses.get(0);

                        String city = a.getLocality();
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
                new Handler(Looper.getMainLooper()).post(() -> callback.onCity(finalResult));

            }).start();
        }

        client.requestLocationUpdates(request, listener, Looper.getMainLooper())
                .addOnFailureListener(e -> callback.onError("request failed: " + e.getMessage()));
    }
}
