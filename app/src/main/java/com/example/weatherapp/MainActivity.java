package com.example.weatherapp;

// MainActivity — single screen. Steps done here: 26 request, 27 save response,
// 28 failures, 29 log, 30 display live temperature.

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;                  // Step 30

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.weatherapp.api.RetrofitClient;
import com.example.weatherapp.api.WeatherApi;
import com.example.weatherapp.models.ApiError;   // Step 28
import com.example.weatherapp.models.WeatherResponse;
import com.google.gson.Gson;                     // Step 28
import com.google.gson.JsonSyntaxException;      // Step 28

import java.io.IOException;                      // Step 28
import java.net.SocketTimeoutException;          // Step 28
import java.net.UnknownHostException;            // Step 28

import okhttp3.ResponseBody;                     // Step 28

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.Locale;
import com.example.weatherapp.utils.WeatherCodeMapper; //step 35
import android.widget.ImageView;                    // Step 36
import com.example.weatherapp.utils.WeatherCodeIconMapper;   // Step 36
import com.example.weatherapp.utils.LocationHelper;   // Step 41

import android.Manifest;                                                        // Step 43
import androidx.activity.result.ActivityResultLauncher;                         // Step 43
import androidx.activity.result.contract.ActivityResultContracts;               // Step 43
import android.annotation.SuppressLint;                    // Step 44
import android.location.Location;                          // Step 44
import android.os.Looper;                                  // Step 44

import com.google.android.gms.location.LocationCallback;   // Step 44
import com.google.android.gms.location.LocationRequest;    // Step 44
import com.google.android.gms.location.LocationResult;     // Step 44
import com.google.android.gms.location.Priority;           // Step 44
import android.os.Handler;                                  // Step 45


public class MainActivity extends AppCompatActivity {

    // Step 26 — Logcat filter name
    private static final String TAG = "WEATHER_TEST";

    // Step 26 — fields to request (must match API names exactly)
    private static final String CURRENT_FIELDS =
            "temperature_2m,relative_humidity_2m,apparent_temperature,"
                    + "is_day,precipitation,weather_code,wind_speed_10m";
    private static final double DEFAULT_LAT = 31.418;    // Step 42: fallback city
    private static final double DEFAULT_LON = 73.079;

    // Step 27 — parsed response; all views read from here
    private WeatherResponse currentWeather;

    // Step 30 — views updated with live data
    private TextView tvTemperature;
    private TextView tvHumidity;                 // Step 31
    private TextView tvFeelsLike;                // Step 32
    private TextView tvWindSpeed;                // Step 33
    private TextView tvPrecipitation;            // Step 34
    private TextView tvCondition;                 //step 35
    private ImageView ivCurrentWeather;              // Step 36: main weather icon
    private ImageView ivHeaderWeather;               // Step 40: small header icon
    private LocationHelper locationHelper;               // Step 41: location helper
    private boolean locationResolved = false;                // Step 45: did GPS answer?



    // Step 43: permission dialog result comes back here
    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {

                        Boolean fine = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                        Boolean coarse = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);

                        boolean granted = Boolean.TRUE.equals(fine)
                                || Boolean.TRUE.equals(coarse);

                        Log.d("LOCATION", "Permission result: " + result);   // Step 43

                        if (granted) {
                            Log.d("LOCATION", "Permission granted — GPS path (step 44)");
                            useDeviceLocation();                     // Step 44
                        } else {
                            Log.d("LOCATION", "Denied — staying on default city");   // Step 45
                            useFallbackLocation("permission denied");                 // Step 45
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        tvTemperature = findViewById(R.id.tvTemperature);   // Step 30
        tvHumidity = findViewById(R.id.tvHumidity);   // Step 31
        tvFeelsLike = findViewById(R.id.tvFeelsLike);   // Step 32
        tvWindSpeed = findViewById(R.id.tvWindSpeed);   // Step 33
        tvPrecipitation = findViewById(R.id.tvPrecipitation);   // Step 34
        tvCondition = findViewById(R.id.tvCondition); //step 35
        ivCurrentWeather = findViewById(R.id.ivCurrentWeather);   // Step 36: find icon view
        ivHeaderWeather = findViewById(R.id.ivHeaderWeather);   // Step 40: find header icon

        locationHelper = new LocationHelper(this);       // Step 41: create helper
        Log.d("LOCATION", "helper ready, permission="    // Step 41: sanity check
                + locationHelper.hasPermission());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

//
//       fetchCurrentWeather(31.418, 73.079);   // (removed this)Faisalabad for now (GPS later: step 48)
        loadWeather();                            // Step 42: check permission first
    }
    /** Step 42: decide where the weather comes from. */
    /** Step 42-43: check permission, ask if missing, then load. */
    private void loadWeather() { //replaced code with step 42

        if (locationHelper.hasPermission()) {
            Log.d("LOCATION", "Permission granted — GPS path (step 44)");
            useDeviceLocation();                             // Step 44   // step 44 replaces
            return;
        }

        Log.d("LOCATION", "No permission — asking the user");   // Step 43
        locationPermissionLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,       // Step 43
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }
    /** Step 44: get GPS coordinates, or fall back to the default city. */
    /** Step 44-45: get GPS coordinates, with a timeout and a fallback city. */
    private void useDeviceLocation() {

        locationResolved = false;                                          // Step 45

        // Step 45: watchdog — if GPS stays silent, fall back after 15 s
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!locationResolved) {
                Log.w("LOCATION", "GPS timed out after 15 s");             // Step 45
                useFallbackLocation("gps timeout");
            }
        }, 15_000L);

        locationHelper.requestLocation(new LocationHelper.Callback() {

            @Override
            public void onLocation(double lat, double lon) {
                locationResolved = true;                                   // Step 45
                Log.d("LOCATION", "GPS: " + lat + ", " + lon);             // Step 44

                fetchCurrentWeather(lat, lon);                             // Step 48 target

                // Step 46: coordinates -> readable city name
                locationHelper.resolveCityName(lat, lon, city ->
                        Log.d("LOCATION", "City: " + city));
            }

            @Override
            public void onError(String reason) {
                locationResolved = true;                                   // Step 45
                useFallbackLocation(reason);                               // Step 45
            }
        });
    }
    /** Step 45: single safety net for every "no location" case. */
    private void useFallbackLocation(String reason) {

        Log.w("LOCATION", "Using default city — " + reason);
        fetchCurrentWeather(DEFAULT_LAT, DEFAULT_LON);
    }
    /** Step 26 — send request off the main thread. */
    private void fetchCurrentWeather(double lat, double lon) {

        WeatherApi api = RetrofitClient.getApi();

        Call<WeatherResponse> call =
                api.getWeather(lat, lon, CURRENT_FIELDS, "", "", "auto", 1);
        //                        hourly ─┘   └─ daily   timezone  forecast_days

        Log.d(TAG, "Request URL: " + call.request().url());

        call.enqueue(new Callback<WeatherResponse>() {

            @Override
            public void onResponse(Call<WeatherResponse> call,
                                   Response<WeatherResponse> response) {

                if (response.isSuccessful()) {
                    handleSuccess(response.body());                          // Step 27
                } else {
                    handleHttpError(response.code(), response.errorBody());  // Step 28
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                handleNetworkFailure(t);                                     // Step 28
            }
        });
    }

    /** Step 27 — validate, store, then draw. */
    private void handleSuccess(WeatherResponse weather) {

        if (weather == null) {
            Log.e(TAG, "Body was empty");
            return;
        }

        if (weather.current == null) {
            Log.e(TAG, "JSON parsed but 'current' is missing");
            return;
        }

        currentWeather = weather;

        logWeather(currentWeather.current);   // Step 29
        renderCurrentWeather();               // Step 30
    }

    /** Step 29 — dump all fields for comparison with the UI. */
    private void logWeather(WeatherResponse.CurrentWeather c) {

        Log.d(TAG, "----- CURRENT WEATHER -----");
        Log.d(TAG, "time          : " + c.time);
        Log.d(TAG, "temperature   : " + c.temperature + " °C");
        Log.d(TAG, "feels like    : " + c.feelsLike + " °C");
        Log.d(TAG, "humidity      : " + c.humidity + " %");
        Log.d(TAG, "wind speed    : " + c.windSpeed + " km/h");
        Log.d(TAG, "precipitation : " + c.precipitation + " mm");
        Log.d(TAG, "weather code  : " + c.weatherCode);
        Log.d(TAG, "is_day        : " + c.isDay + " (" + (c.isDay == 1 ? "day" : "night") + ")");
        Log.d(TAG, "---------------------------");
    }

    /** Step 30 — paint stored data onto the screen. */
    private void renderCurrentWeather() {

        if (currentWeather == null || currentWeather.current == null) {
            Log.e(TAG, "renderCurrentWeather called with no data");
            return;
        }

        WeatherResponse.CurrentWeather c = currentWeather.current;

        tvTemperature.setText(Math.round(c.temperature) + "°");
        tvHumidity.setText(c.humidity + "%");      // Step 31
        tvFeelsLike.setText("Feels like " + Math.round(c.feelsLike) + "°");   // Step 32

        tvCondition.setText(WeatherCodeMapper.describe(c.weatherCode));   // Step 35
        tvWindSpeed.setText(String.format(Locale.US, "%.1f km/h", c.windSpeed));   // Step 33
        tvPrecipitation.setText(String.format(Locale.US, "%.1f mm", c.precipitation));   // Step 34



//        ivCurrentWeather.setImageResource(                            // Step 36: code -> icon
//                WeatherCodeIconMapper.iconFor(95, false));   // test: strom

        int icon = WeatherCodeIconMapper.iconFor(c.weatherCode, c.isDay == 1);   // Step 40

        ivCurrentWeather.setImageResource(icon);        // Step 40: big card icon
        ivHeaderWeather.setImageResource(icon);         // Step 40: header icon

    }


    /** Step 28 — server refused (HTTP 4xx/5xx); read its reason. */
    private void handleHttpError(int code, ResponseBody errorBody) {

        String body = "no error body";

        if (errorBody != null) {
            try {
                body = errorBody.string();       // one-shot stream
            } catch (IOException e) {
                body = "<unreadable>";
            }
        }

        Log.e(TAG, "HTTP " + code + " — " + body);

        ApiError parsed = null;
        try {
            parsed = new Gson().fromJson(body, ApiError.class);
        } catch (JsonSyntaxException ignored) {
            // not JSON
        }

        if (parsed != null && parsed.reason != null) {
            Log.e(TAG, "API reason: " + parsed.reason);
        }

        if (code == 400) {
            Log.e(TAG, "Bad request — URL or field name is wrong");
        } else if (code == 429) {
            Log.e(TAG, "Too many requests — wait a bit");
        } else if (code >= 500) {
            Log.e(TAG, "Server-side problem at Open-Meteo");
        }
    }

    /** Step 28 — never reached the server. */
    private void handleNetworkFailure(Throwable t) {

        if (t instanceof UnknownHostException) {
            Log.e(TAG, "No internet or DNS failure", t);
        } else if (t instanceof SocketTimeoutException) {
            Log.e(TAG, "Request timed out", t);
        } else if (t instanceof IOException) {
            Log.e(TAG, "I/O problem during request", t);
        } else {
            Log.e(TAG, "Unexpected failure", t);
        }
    }
}