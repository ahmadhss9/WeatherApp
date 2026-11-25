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
import android.content.SharedPreferences;                                       // Step 60
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
import android.widget.EditText;                                          // Step 54
import com.google.android.material.button.MaterialButton;                // Step 54 (or use FQN as btnRefresh does)
import com.google.android.material.textfield.TextInputEditText;          // Step 54
import android.view.inputmethod.EditorInfo;                        // Step 55
import android.view.inputmethod.InputMethodManager;                // Step 57
import android.view.LayoutInflater;                                // Step 56
import android.view.View;                                          // Step 56
import android.widget.LinearLayout;                                // Step 56
import com.example.weatherapp.models.GeocodingResponse;            // Step 55
import java.util.List;                                             // Step 56
import androidx.recyclerview.widget.LinearLayoutManager;           // Step 63
import androidx.recyclerview.widget.RecyclerView;                  // Step 63
import com.example.weatherapp.adapters.HourlyAdapter;              // Step 63
import com.example.weatherapp.models.HourlyItem;                   // Step 63
import java.util.ArrayList;                                        // Step 64


public class MainActivity extends AppCompatActivity {

    // Step 26 — Logcat filter name
    private static final String TAG = "WEATHER_TEST";

    // Step 26 — fields to request (must match API names exactly)
    private static final String CURRENT_FIELDS =
            "temperature_2m,relative_humidity_2m,apparent_temperature,"
                    + "is_day,precipitation,weather_code,wind_speed_10m";

    // Step 62 — hourly fields (parallel arrays, one value per hour)
    private static final String HOURLY_FIELDS =
            "temperature_2m,weather_code,is_day";
    private static final double DEFAULT_LAT = 31.418;    // Step 42: fallback city
    private static final double DEFAULT_LON = 73.079;

    // ---- Step 59: where the chosen city is remembered ----
    private static final String PREFS = "weather_prefs";          // Step 59
    private static final String KEY_CITY_LABEL = "city_label";    // Step 59: "London, England, UK"
    private static final String KEY_CITY_LAT = "city_lat";        // Step 59
    private static final String KEY_CITY_LON = "city_lon";        // Step 59

    // Step 27 — parsed response; all views read from here
    private WeatherResponse currentWeather;

    // Step 30 — views updated with live data
    private TextView tvTemperature;
    private TextView tvHumidity;                 // Step 31
    private TextView tvFeelsLike;                // Step 32
    private TextView tvWindSpeed;                // Step 33
    private TextView tvPrecipitation;            // Step 34
    private TextView tvCondition;                 //step 35
    private TextView tvLocation;                     // Step 47: detected city label
    private ImageView ivCurrentWeather;              // Step 36: main weather icon
    private ImageView ivHeaderWeather;               // Step 40: small header icon
    private LocationHelper locationHelper;               // Step 41: location helper
    private boolean locationResolved = false;                // Step 45: did GPS answer?

    private boolean hadPermission;              // Step 50: permission state at last check
    private com.google.android.material.button.MaterialButton btnRefresh;   // Step 49: refresh button
    private double lastLat = DEFAULT_LAT;            // Step 48: coords last requested
    private double lastLon = DEFAULT_LON;

    private com.google.android.material.button.MaterialButton btnSearch;      // Step 54
    private TextInputEditText etCitySearch;                                   // Step 54
    private LinearLayout llSearchResults;                                     // Step 56: results container
    private GeocodingResponse.Result selectedCity;                            // Step 57: chosen city
    private RecyclerView rvHourly;                                            // Step 64: hourly strip
    private final HourlyAdapter hourlyAdapter = new HourlyAdapter();          // Step 63-64


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
                            // Step 50: "denied once" vs "never ask again"
                            boolean canAskAgain = shouldShowRequestPermissionRationale(
                                    Manifest.permission.ACCESS_FINE_LOCATION);

                            Log.w("LOCATION", canAskAgain
                                    ? "Denied — will ask again next launch"
                                    : "Denied permanently — enable in Settings");    // Step 50

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
        tvLocation = findViewById(R.id.tvLocation);  // Step 47
        btnRefresh = findViewById(R.id.btnRefresh);          // Step 49
        btnRefresh.setOnClickListener(v -> refreshWeather()); // Step 49
        tvWindSpeed = findViewById(R.id.tvWindSpeed);   // Step 33
        tvPrecipitation = findViewById(R.id.tvPrecipitation);   // Step 34
        tvCondition = findViewById(R.id.tvCondition); //step 35
        ivCurrentWeather = findViewById(R.id.ivCurrentWeather);   // Step 36: find icon view
        ivHeaderWeather = findViewById(R.id.ivHeaderWeather);   // Step 40: find header icon
        locationHelper = new LocationHelper(this);       // Step 41: create helper
        Log.d("LOCATION", "helper ready, permission="    // Step 41: sanity check
                + locationHelper.hasPermission());

        hadPermission = locationHelper.hasPermission();   // Step 50: remember current state

        btnSearch = findViewById(R.id.btnSearch);                    // Step 54
        etCitySearch = findViewById(R.id.etCitySearch);              // Step 54
        llSearchResults = findViewById(R.id.llSearchResults);        // Step 56

        rvHourly = findViewById(R.id.rvHourly);                      // Step 64
        rvHourly.setLayoutManager(                                   // Step 64: horizontal list
                new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        rvHourly.setAdapter(hourlyAdapter);                          // Step 63-64
        btnSearch.setOnClickListener(v -> onSearchClicked());        // Step 54

        etCitySearch.setOnEditorActionListener((v, actionId, event) -> {   // Step 55
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {                // keyboard search key
                onSearchClicked();
                return true;
            }
            return false;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

//
//       fetchCurrentWeather(31.418, 73.079);   // (removed this)Faisalabad for now (GPS later: step 48)
        if (restoreSavedCity()) {                 // Step 60: saved city wins
            loadSelectedCityWeather();            // Step 60: weather for the saved city
        } else {
            loadWeather();                        // Step 42: check permission first
        }
    }

    /** Step 50: notice permission changes made in Settings while we were paused. */
    @Override
    protected void onResume() {
        super.onResume();

        boolean now = locationHelper.hasPermission();      // Step 50

        if (now && !hadPermission) {
            Log.d("LOCATION", "Permission granted while paused — reloading");   // Step 50

            if (selectedCity == null) {
                loadWeather();                          // no chosen city: go GPS
            } else {
                Log.d("GEOCODE", "Keeping chosen city: " + selectedCity.label());  // Step 60
                loadSelectedCityWeather();              // a chosen city wins over GPS
            }
        } else if (!now && hadPermission) {
            Log.d("LOCATION", "Permission revoked while paused");               // Step 50
        }

        hadPermission = now;                               // Step 50
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

                fetchCurrentWeather(lat, lon);                             // Step 48: weather at GPS coords
                showCity(lat, lon);                                        // Steps 46-47: city label on screen
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
        showCity(DEFAULT_LAT, DEFAULT_LON);          // Step 47: label the fallback city
    }
    /** Steps 49+58: refresh the chosen city, else GPS, else the last coordinates. */
    private void refreshWeather() {

        Log.d("LOCATION", "Refresh pressed");                // Step 49
        btnRefresh.setEnabled(false);                        // Step 49: block double taps

        if (selectedCity != null) {
            loadSelectedCityWeather();                       // Step 58: stay on the chosen city
        } else if (locationHelper.hasPermission()) {
            useDeviceLocation();                             // Step 49: fresh GPS + refetch
        } else {
            fetchCurrentWeather(lastLat, lastLon);           // Step 48 coords
            showCity(lastLat, lastLon);                      // keep header accurate
        }
    }
    /** Step 54-55: read the input and start a search. */
    private void onSearchClicked() {

        String query = etCitySearch.getText() == null
                ? "" : etCitySearch.getText().toString().trim();      // Step 54

        performSearch(query);                                         // Step 55
    }

    /** Step 55: ask the geocoding API for cities matching the typed text. */
    private void performSearch(String query) {

        if (query.isEmpty()) {
            Log.w("GEOCODE", "Empty query — nothing to search");       // step 75 adds UI
            return;
        }

        btnSearch.setEnabled(false);                                   // Step 55: block double taps
        hideSearchResults();                                           // Step 56: clear old list
        Log.d("GEOCODE", "Searching: " + query);

        Call<GeocodingResponse> call =
                RetrofitClient.getGeocodingApi()
                        .searchCities(query, 5, "en", "json");         // Step 55

        Log.d("GEOCODE", "Request URL: " + call.request().url());      // Step 55

        call.enqueue(new Callback<GeocodingResponse>() {

            @Override
            public void onResponse(Call<GeocodingResponse> call,
                                   Response<GeocodingResponse> response) {

                btnSearch.setEnabled(true);                            // Step 55

                if (!response.isSuccessful()) {
                    Log.e("GEOCODE", "HTTP " + response.code());       // step 72 shows it
                    return;
                }

                GeocodingResponse body = response.body();

                if (body == null || body.results == null || body.results.isEmpty()) {
                    Log.w("GEOCODE", "No matching city for: " + query); // Step 75 empty state
                    hideSearchResults();                               // Step 56
                    return;
                }

                Log.d("GEOCODE", "Matches: " + body.results.size());   // Step 55

                for (GeocodingResponse.Result r : body.results) {
                    Log.d("GEOCODE", "  " + r.label()
                            + "  (" + r.latitude + ", " + r.longitude + ")");
                }

                showSearchResults(body.results);                       // Step 56: draw the list
            }

            @Override
            public void onFailure(Call<GeocodingResponse> call, Throwable t) {
                btnSearch.setEnabled(true);                            // Step 55
                Log.e("GEOCODE", "Search failed: " + t);               // Step 74 offline case
            }
        });
    }
    /** Step 56: draw one tappable row per matching city. */
    private void showSearchResults(List<GeocodingResponse.Result> results) {

        llSearchResults.removeAllViews();                              // Step 56: no stale rows

        if (results == null || results.isEmpty()) {                    // Step 56
            llSearchResults.setVisibility(View.GONE);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);            // Step 56
        llSearchResults.setVisibility(View.VISIBLE);                   // Step 56

        for (GeocodingResponse.Result r : results) {

            View row = inflater.inflate(R.layout.item_city_result, llSearchResults, false);

            TextView tvName = row.findViewById(R.id.tvCityName);       // Step 56
            TextView tvCoords = row.findViewById(R.id.tvCityCoords);   // Step 56

            tvName.setText(r.label());                                 // Step 56: "Lahore, Punjab, Pakistan"
            tvCoords.setText(String.format(Locale.US, "%.2f, %.2f",   // Step 56
                    r.latitude, r.longitude));

            row.setOnClickListener(v -> onCitySelected(r));            // Step 57: pick this city

            llSearchResults.addView(row);                              // Step 56
        }
    }

    /** Step 56: collapse and empty the results list. */
    private void hideSearchResults() {

        llSearchResults.removeAllViews();                              // Step 56
        llSearchResults.setVisibility(View.GONE);                      // Step 56
    }

    /** Step 57: the user tapped one of the matching cities. */
    private void onCitySelected(GeocodingResponse.Result city) {

        Log.d("GEOCODE", "Selected: " + city.label()
                + "  (" + city.latitude + ", " + city.longitude + ")");   // Step 57

        selectedCity = city;                                          // Step 57: remember the choice
        saveSelectedCity(city);                                       // Step 59: persist it

        hideSearchResults();                                          // Step 57
        hideKeyboard();                                               // Step 57

        tvLocation.setText(city.label());                             // Step 57: header shows the choice
        etCitySearch.setText(city.name);                              // Step 57: keep the name, not the region
        etCitySearch.clearFocus();                                    // Step 57: no cursor blinking

        loadSelectedCityWeather();                                    // Step 58: fetch this city
    }

    /** Step 58: request weather for the city the user picked. */
    private void loadSelectedCityWeather() {

        if (selectedCity == null) {
            Log.w("GEOCODE", "loadSelectedCityWeather: no city selected");
            return;
        }

        Log.d("GEOCODE", "Loading weather for " + selectedCity.label());     // Step 58

        fetchCurrentWeather(selectedCity.latitude, selectedCity.longitude);  // Step 58
    }

    /** Step 59: remember the chosen city so step 60 can restore it. */
    private void saveSelectedCity(GeocodingResponse.Result city) {

        // SharedPreferences has no putDouble(), so store the bits of the double
        getSharedPreferences(PREFS, MODE_PRIVATE)                     // Step 59
                .edit()
                .putString(KEY_CITY_LABEL, city.label())              // Step 59
                .putLong(KEY_CITY_LAT, Double.doubleToRawLongBits(city.latitude))    // Step 59
                .putLong(KEY_CITY_LON, Double.doubleToRawLongBits(city.longitude))   // Step 59
                .apply();                                            // Step 59: async write

        Log.d("GEOCODE", "Saved city: " + city.label());              // Step 59
    }

    /** Step 60: rebuild the saved city on launch (true = we have one). */
    private boolean restoreSavedCity() {

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);   // Step 60

        String label = prefs.getString(KEY_CITY_LABEL, null);                  // Step 60

        if (label == null
                || !prefs.contains(KEY_CITY_LAT)
                || !prefs.contains(KEY_CITY_LON)) {
            Log.d("GEOCODE", "No saved city");                                 // Step 60
            return false;
        }

        GeocodingResponse.Result city = new GeocodingResponse.Result();        // Step 60
        city.name = label;                                                     // "Tokyo, Tokyo, Japan"
        city.latitude = Double.longBitsToDouble(prefs.getLong(KEY_CITY_LAT, 0L));   // Step 60
        city.longitude = Double.longBitsToDouble(prefs.getLong(KEY_CITY_LON, 0L));  // Step 60

        selectedCity = city;                                                   // Step 60

        tvLocation.setText(label);                                             // Step 60: header right away
        etCitySearch.setText(label);                                           // Step 60: field too

        Log.d("GEOCODE", "Restored city: " + label);                           // Step 60
        return true;
    }

    /** Step 57: dismiss the soft keyboard after a selection. */
    private void hideKeyboard() {

        View focus = getCurrentFocus();                               // Step 57
        if (focus == null) return;

        InputMethodManager imm =
                (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);   // Step 57

        if (imm != null) {
            imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);   // Step 57
        }
    }

    /** Step 47: geocode the coordinates and write the city on screen. */
    private void showCity(double lat, double lon) {

        locationHelper.resolveCityName(lat, lon, city -> {

            Log.d("LOCATION", "City: " + city);                          // Step 46

            tvLocation.setText(city != null ? city : "Unknown location"); // Step 47
        });
    }


    /** Step 26 — send request off the main thread. */
    private void fetchCurrentWeather(double lat, double lon) {
        lastLat = lat;                               // Step 48: remember for refresh
        lastLon = lon;

        WeatherApi api = RetrofitClient.getApi();

        Call<WeatherResponse> call =
                api.getWeather(lat, lon, CURRENT_FIELDS, HOURLY_FIELDS, "", "auto", 2);
        //                                  current ─┘   hourly ─┘   daily  tz      forecast_days=2
        //  2 days so the "next 24 hours" strip still has data after midnight (step 64)

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
        btnRefresh.setEnabled(true);          // Step 49: re-arm the refresh button

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
        logHourly();                          // Step 62
        renderCurrentWeather();               // Step 30
        renderHourly();                       // Step 64
    }

    /** Step 64: fill the horizontal strip with the next 24 hours. */
    private void renderHourly() {

        if (currentWeather == null || currentWeather.hourly == null
                || currentWeather.hourly.size() == 0) {
            hourlyAdapter.submitList(null);                        // Step 64: clear the strip
            Log.w(TAG, "renderHourly: no usable hourly data");
            return;
        }

        WeatherResponse.Hourly h = currentWeather.hourly;
        int hours = h.size();                                      // Step 61: safe length

        // Step 64: start at the hour we are in, using the API's own clock
        int start = 0;
        if (currentWeather.current != null && currentWeather.current.time != null) {
            String nowPrefix = currentWeather.current.time.substring(0, 13);   // "2026-11-25T10"
            for (int i = 0; i < hours; i++) {
                if (h.time.get(i).startsWith(nowPrefix)) {
                    start = i;
                    break;
                }
            }
        }

        int end = Math.min(start + 24, hours);                     // Step 64: at most 24 cells

        // Step 65: fallback for hours whose is_day is missing
        boolean dayFallback = currentWeather.current != null
                && currentWeather.current.isDay == 1;

        List<HourlyItem> items = new ArrayList<>();                // Step 64

        for (int i = start; i < end; i++) {

            boolean isDay = h.isDay != null && i < h.isDay.size()   // Step 65
                    ? h.isDay.get(i) == 1
                    : dayFallback;

            items.add(new HourlyItem(
                    hourLabel(h.time.get(i), i == start),          // "NOW" / "10 PM"
                    h.temperature.get(i),
                    h.weatherCode.get(i),
                    isDay,
                    i == start));                                  // Step 64

            if (i == start) {                                      // Step 65: verify the icon choice
                Log.d(TAG, "First hour icon: " + getResources().getResourceEntryName(
                        WeatherCodeIconMapper.iconFor(h.weatherCode.get(i), isDay)));
            }
        }

        hourlyAdapter.submitList(items);                           // Step 64
        Log.d(TAG, "Hourly cells: " + items.size() + " (start index " + start + ")");
    }

    /** Step 64: "2026-11-25T22:00" -> "NOW" or "10 PM". */
    private String hourLabel(String isoTime, boolean isNow) {

        if (isNow) return "NOW";                                   // Step 64

        try {
            int hour = Integer.parseInt(isoTime.substring(11, 13));   // "22"
            int hour12 = hour % 12;
            if (hour12 == 0) hour12 = 12;                          // 0 -> 12 AM, 12 -> 12 PM
            return hour12 + (hour < 12 ? " AM" : " PM");           // Step 64
        } catch (RuntimeException e) {
            return isoTime;                                        // unexpected format: show raw
        }
    }

    /** Step 62: prove the hourly arrays parsed correctly. */
    private void logHourly() {

        if (currentWeather == null || currentWeather.hourly == null) {
            Log.w(TAG, "No hourly block in the response");   // Step 62
            return;
        }

        WeatherResponse.Hourly h = currentWeather.hourly;
        int hours = h.size();                                // Step 61: safe length

        Log.d(TAG, "----- HOURLY (" + hours + " hours) -----");   // Step 62

        for (int i = 0; i < Math.min(hours, 6); i++) {
            Log.d(TAG, h.time.get(i)
                    + "  " + h.temperature.get(i) + " °C"
                    + "  code=" + h.weatherCode.get(i)
                    + "  is_day=" + h.isDay.get(i));          // Step 62
        }
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
        btnRefresh.setEnabled(true); // Step 49

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

        btnRefresh.setEnabled(true); // Step 49

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