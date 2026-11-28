package com.example.weatherapp;

// MainActivity — single screen. Steps done here: 26 request, 27 save response,
// 28 failures, 29 log, 30 display live temperature.

import android.os.Bundle;

import android.text.TextWatcher;
import android.text.Editable;
import android.util.Log;
import android.os.Handler;
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
import com.example.weatherapp.BuildConfig;       // Step 91: VERSION_NAME at runtime

import java.io.IOException;                      // Step 28
import java.net.SocketTimeoutException;          // Step 28
import java.net.UnknownHostException;            // Step 28

import okhttp3.ResponseBody;                     // Step 28

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.Locale;
import com.example.weatherapp.utils.WeatherCodeMapper; //step 35
import android.widget.ImageButton;                                       // UI redesign: search lens button
import android.widget.ImageView;                    // Step 36
import com.example.weatherapp.utils.WeatherCodeIconMapper;   // Step 36
import com.example.weatherapp.utils.LocationHelper;   // Step 41

import android.Manifest;                                                        // Step 43
import android.content.res.ColorStateList;                                       // Step 87
import android.graphics.drawable.Drawable;                                     // Step 79
import androidx.core.view.WindowCompat;                                        // Step 79
import androidx.core.view.WindowInsetsControllerCompat;                        // Step 79
import android.content.SharedPreferences;                                       // Step 60
import android.net.ConnectivityManager;                                        // Step 74
import android.net.Network;                                                    // Step 74
import android.net.NetworkCapabilities;                                        // Step 74
import androidx.activity.result.ActivityResultLauncher;                         // Step 43
import androidx.activity.result.contract.ActivityResultContracts;               // Step 43
import android.os.Looper;                                  // Step 44
import android.os.Handler;                                  // Step 45
import android.widget.ProgressBar;                                      // Step 71
import com.google.android.material.button.MaterialButton;                // Step 54 + 90
import com.google.android.material.textfield.TextInputEditText;          // Step 54
import com.google.android.material.textfield.TextInputLayout;            // Step 87
import android.view.inputmethod.EditorInfo;                        // Step 55
import android.view.MotionEvent;                                   // Step 77
import android.widget.ScrollView;                                  // Step 77
import android.view.inputmethod.InputMethodManager;                // Step 57
import android.view.LayoutInflater;                                // Step 56
import android.view.View;                                          // Step 56
import android.widget.LinearLayout;                                // Step 56
import com.example.weatherapp.models.GeocodingResponse;            // Step 55
import com.example.weatherapp.models.IpLocationResponse;           // Step 76
import java.util.List;                                             // Step 56
import androidx.recyclerview.widget.LinearLayoutManager;           // Step 63
import androidx.recyclerview.widget.RecyclerView;                  // Step 63
import com.example.weatherapp.adapters.HourlyAdapter;              // Step 63
import com.example.weatherapp.models.HourlyItem;                   // Step 63
import java.util.ArrayList;                                        // Step 64
import androidx.core.content.ContextCompat;                        // Step 69
import androidx.recyclerview.widget.DividerItemDecoration;         // Step 69
import com.example.weatherapp.adapters.DailyAdapter;               // Step 68
import com.example.weatherapp.models.DailyItem;                    // Step 69
import java.text.ParseException;                                   // Step 69
import java.text.SimpleDateFormat;                                 // Step 69
import java.util.Date;                                             // Step 69


public class MainActivity extends AppCompatActivity {

    // Step 26 + 88 — Logcat filter name (cleaned from WEATHER_TEST)
    private static final String TAG = "WEATHER";

    // Step 26 — fields to request (must match API names exactly)
    private static final String CURRENT_FIELDS =
            "temperature_2m,relative_humidity_2m,apparent_temperature,"
                    + "is_day,precipitation,weather_code,wind_speed_10m";

    // Step 62 — hourly fields (parallel arrays, one value per hour)
    private static final String HOURLY_FIELDS =
            "temperature_2m,weather_code,is_day";

    // Step 67 — daily fields (parallel arrays, one value per day)
    private static final String DAILY_FIELDS =
            "weather_code,temperature_2m_max,temperature_2m_min";
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
    private TextView tvHighLow;                      // Step 70: today's high / low
    private TextView tvError;                        // Step 72: error banner
    private LinearLayout llError;                    // Step 73: banner container
    private MaterialButton btnRetry;                   // Step 73 + 90: retry button
    private String lastSearchQuery;                  // Step 73: what to retry
    private boolean lastFailedSearch;                // Step 73: which op failed
    private boolean nightTheme = false;              // Step 79: current palette
    private ImageView ivCurrentWeather;              // Step 36: main weather icon
    private ImageView ivHeaderWeather;               // Step 40: small header icon
    private LocationHelper locationHelper;               // Step 41: location helper
    private boolean locationResolved = false;                // Step 45: did GPS answer?

    private boolean hadPermission;              // Step 50: permission state at last check
    private ImageButton btnToggleSearch;             // UI redesign: search lens button in header
    private LinearLayout llSearchBar;                // UI redesign: collapsible search bar
    private double lastLat = DEFAULT_LAT;            // Step 48: coords last requested
    private double lastLon = DEFAULT_LON;

    private MaterialButton btnSearch;                // Step 54 + 90
    private TextInputLayout searchLayout;            // Step 87: themed search box
    private TextInputEditText etCitySearch;                                   // Step 54
    private LinearLayout llSearchResults;                                     // Step 56: results container
    private GeocodingResponse.Result selectedCity;                            // Step 57: chosen city
    private RecyclerView rvHourly;                                            // Step 64: hourly strip
    private final HourlyAdapter hourlyAdapter = new HourlyAdapter();          // Step 63-64
    private RecyclerView rvDaily;                                             // Step 69: 7-day list
    private final DailyAdapter dailyAdapter = new DailyAdapter();             // Step 68-69
    private ProgressBar progressBar;                                          // Step 71: loading spinner
    private int activeRequests = 0;                                           // Step 71: weather + search
    private TextView tvVersion;                                               // Step 91: version footer


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
        tvHighLow = findViewById(R.id.tvHighLow);    // Step 70
        progressBar = findViewById(R.id.progressBar);// Step 71
        tvError = findViewById(R.id.tvError);        // Step 72
        llError = findViewById(R.id.llError);        // Step 73
        btnRetry = findViewById(R.id.btnRetry);      // Step 73
        btnRetry.setOnClickListener(v -> retryLastRequest());   // Step 73

        setupPullToRefresh();                            // Step 77: pull down anywhere at top to refresh
        tvWindSpeed = findViewById(R.id.tvWindSpeed);   // Step 33
        tvPrecipitation = findViewById(R.id.tvPrecipitation);   // Step 34
        tvCondition = findViewById(R.id.tvCondition); //step 35
        ivCurrentWeather = findViewById(R.id.ivCurrentWeather);   // Step 36: find icon view
        ivHeaderWeather = findViewById(R.id.ivHeaderWeather);   // Step 40: find header icon
        locationHelper = new LocationHelper(this);       // Step 41: create helper
        Log.d("LOCATION", "helper ready, permission="    // Step 41: sanity check
                + locationHelper.hasPermission());

        hadPermission = locationHelper.hasPermission();   // Step 50: remember current state

        btnToggleSearch = findViewById(R.id.btnToggleSearch);        // UI redesign: header search lens
        llSearchBar = findViewById(R.id.llSearchBar);                // UI redesign: hidden until tapped
        btnSearch = findViewById(R.id.btnSearch);                    // Step 54
        searchLayout = findViewById(R.id.searchLayout);              // Step 87
        etCitySearch = findViewById(R.id.etCitySearch);              // Step 54
        llSearchResults = findViewById(R.id.llSearchResults);        // Step 56


        // Add live suggestion TextWatcher with debounce (300ms)
        final Handler searchHandler = new Handler(Looper.getMainLooper());
        final Runnable[] pendingSearch = {null};
        etCitySearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                // Cancel previous pending search
                if (pendingSearch[0] != null) searchHandler.removeCallbacks(pendingSearch[0]);
                // Schedule new search after delay
                pendingSearch[0] = () -> {
                    String query = s.toString().trim();
                    if (!query.isEmpty()) {
                        performSearch(query);
                    }
                };
                searchHandler.postDelayed(pendingSearch[0], 300);
            }
        });

        rvHourly = findViewById(R.id.rvHourly);                      // Step 64
        rvHourly.setLayoutManager(                                   // Step 64: horizontal list
                new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        rvHourly.setAdapter(hourlyAdapter);                          // Step 63-64
        rvHourly.setHasFixedSize(true);                              // Step 78: fixed width cells
        rvHourly.setNestedScrollingEnabled(false);                   // Step 78: vertical scroll is the page's job

        rvDaily = findViewById(R.id.rvDaily);                        // Step 69
        rvDaily.setLayoutManager(new LinearLayoutManager(this));     // Step 69: vertical
        rvDaily.setAdapter(dailyAdapter);                            // Step 68-69
        rvDaily.setVisibility(View.VISIBLE); // Ensure daily list is visible
        rvDaily.setHasFixedSize(false);                              // Step 78 + 87: wrap_content height must re-measure on update
        rvDaily.setNestedScrollingEnabled(false);                    // Step 78: no nested vertical scroll

        DividerItemDecoration divider =                              // Step 69: row separators
                new DividerItemDecoration(this, RecyclerView.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider_daily));
        rvDaily.addItemDecoration(divider);                          // Step 69
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

        // Step 88: temporary hardcoded launch call removed
        // Step 91: bind version footer — reads BuildConfig.VERSION_NAME so the string
        //          stays in sync with versionName in build.gradle.kts automatically
        tvVersion = findViewById(R.id.tvVersion);
        if (tvVersion != null) {
            tvVersion.setText("v" + BuildConfig.VERSION_NAME);   // Step 91: e.g. "v1.0"
        }

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
    /** Steps 45+76: no GPS -> try IP location -> only then the hardcoded city. */
    private void useFallbackLocation(String reason) {

        Log.w("LOCATION", "GPS unavailable — " + reason);       // Step 45
        fetchIpLocation(reason);                                // Step 76: try IP first
    }

    /** Step 76: approximate location from the public IP address. */
    private void fetchIpLocation(String reason) {

        if (!isOnline()) {                                      // Step 74
            useDefaultCity(reason + " + offline");
            return;
        }

        Log.d("LOCATION", "IP fallback started (" + reason + ")");   // Step 76

        Call<IpLocationResponse> call = RetrofitClient.getIpLocationApi().getLocation();

        enqueueApiCall("LOCATION", call, new ApiResponseHandler<IpLocationResponse>() {   // Step 89: shared Retrofit wrapper

            @Override
            public void onSuccess(IpLocationResponse body) {
                if (body == null || !body.success
                        || (body.latitude == 0.0 && body.longitude == 0.0)) {
                    Log.w("LOCATION", "IP fallback failed");         // Step 76
                    useDefaultCity(reason + " + ip failed");
                    return;
                }

                String label = ipLabel(body);                        // Step 76
                Log.d("LOCATION", "IP location: " + label
                        + " (" + body.latitude + ", " + body.longitude + ")");

                // after fetching IP location, always show something in the header
                if (label != null) {
                    tvLocation.setText(label);                       // Step 76: approximate city
                } else {
                    // fallback to coordinates if the label is missing
                    tvLocation.setText(String.format(Locale.US, "%.2f, %.2f", body.latitude, body.longitude));
                }

                fetchCurrentWeather(body.latitude, body.longitude);  // Step 76: weather for IP coords
            }

            @Override
            public void onHttpError(int code, ResponseBody errorBody) {
                Log.w("LOCATION", "IP fallback HTTP " + code);       // Step 76 + 89
                useDefaultCity(reason + " + ip failed");
            }

            @Override
            public void onNetworkError(Throwable t) {
                Log.w("LOCATION", "IP fallback network failure: " + t);
                useDefaultCity(reason + " + ip error");              // Step 76
            }
        });
    }

    /** Step 76: "Faisalabad Sadar Tehsil, Punjab, Pakistan". */
    private String ipLabel(IpLocationResponse b) {

        StringBuilder sb = new StringBuilder();

        if (b.city != null && !b.city.isEmpty()) sb.append(b.city);

        if (b.region != null && !b.region.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(b.region);
        }

        if (b.country != null && !b.country.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(b.country);
        }

        return sb.length() == 0 ? null : sb.toString();
    }

    /** Step 76: last resort — the hardcoded city in the constants. */
    private void useDefaultCity(String reason) {

        Log.w("LOCATION", "Using default city — " + reason);     // Step 45-76
        fetchCurrentWeather(DEFAULT_LAT, DEFAULT_LON);
        showCity(DEFAULT_LAT, DEFAULT_LON);                      // Step 47: label it
    }
    /** Steps 49+58: refresh the chosen city, else GPS, else the last coordinates. */
    private void refreshWeather() {

        Log.d("LOCATION", "Refresh pressed");                // Step 49
        // Step 71: beginRequest() inside fetchCurrentWeather now handles the spinner

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

        hideSearchResults();                                           // Step 56: clear old list
        Log.d("GEOCODE", "Searching: " + query);

        lastSearchQuery = query;                                       // Step 73: remember for retry
        lastFailedSearch = false;                                      // Step 73

        if (!isOnline()) {                                             // Step 74: fail fast
            lastFailedSearch = true;                                   // Step 73: retry should re-search
            Log.w("GEOCODE", "Offline — skipping city search");
            showError("You're offline — city search needs an internet connection.");   // Step 74
            return;                                                    // Step 86 + 89: no request started
        }

        Call<GeocodingResponse> call =
                RetrofitClient.getGeocodingApi()
                        .searchCities(query, 5, "en", "json");         // Step 55

        enqueueApiCall("GEOCODE", call, new ApiResponseHandler<GeocodingResponse>() {   // Step 89: shared Retrofit wrapper

            @Override
            public void onSuccess(GeocodingResponse body) {
                if (body == null || body.results == null || body.results.isEmpty()) {
                    Log.w("GEOCODE", "No matching city for: " + query); // Step 75 empty state
                    showNoCityFound(query);                            // Step 75: tell the user
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
            public void onHttpError(int code, ResponseBody errorBody) {
                Log.e("GEOCODE", "HTTP " + code);                      // step 72 shows it
                lastFailedSearch = true;                               // Step 73
                showError("City search failed (HTTP " + code
                        + "). Tap Retry.");                            // Step 72-73
            }

            @Override
            public void onNetworkError(Throwable t) {
                Log.e("GEOCODE", "Search failed: " + t);               // Step 74 offline case
                lastFailedSearch = true;                               // Step 73
                showError("City search failed — check your internet and try again.");   // Step 72
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

        tvName.setTextColor(nightTheme ? 0xFFE6EDF7 : 0xFF172B4D);   // Step 79: readable on dark cards
        tvCoords.setTextColor(nightTheme ? 0xFF94A3B8 : 0xFF8493A8); // Step 79

            row.setOnClickListener(v -> onCitySelected(r));            // Step 57: pick this city

            llSearchResults.addView(row);                              // Step 56
        }
    }

    /** Step 56: collapse and empty the results list. */
    private void hideSearchResults() {

        llSearchResults.removeAllViews();                              // Step 56
        llSearchResults.setVisibility(View.GONE);                      // Step 56
    }

    /** Step 75: nothing matched — show a friendly empty state in the results card. */
    private void showNoCityFound(String query) {

        llSearchResults.removeAllViews();                              // Step 75

        View row = LayoutInflater.from(this)
                .inflate(R.layout.item_city_empty, llSearchResults, false);   // Step 75

        TextView title = row.findViewById(R.id.tvEmptyTitle);          // Step 75
        title.setText("No city found for \"" + query + "\"");          // Step 75
        title.setTextColor(nightTheme ? 0xFFE6EDF7 : 0xFF172B4D);      // Step 79

        llSearchResults.addView(row);                                  // Step 75
        llSearchResults.setVisibility(View.VISIBLE);                   // Step 75
    }

    /** UI redesign: toggle the collapsible search bar from the header search lens. */
    private void toggleSearchBar() {
        boolean show = llSearchBar.getVisibility() != View.VISIBLE;
        setSearchBarVisible(show);
    }

    /** UI redesign: show or collapse the search bar and manage focus/keyboard cleanly. */
    private void setSearchBarVisible(boolean visible) {
        if (visible) {
            llSearchBar.setVisibility(View.VISIBLE);
            etCitySearch.requestFocus();
            InputMethodManager imm =
                    (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etCitySearch, InputMethodManager.SHOW_IMPLICIT);
            }
        } else {
            llSearchBar.setVisibility(View.GONE);
            hideSearchResults();
            etCitySearch.clearFocus();
            hideKeyboard();
        }
    }

    /** Step 57: the user tapped one of the matching cities. */
    private void onCitySelected(GeocodingResponse.Result city) {

        Log.d("GEOCODE", "Selected: " + city.label()
                + "  (" + city.latitude + ", " + city.longitude + ")");   // Step 57

        selectedCity = city;                                          // Step 57: remember the choice
        saveSelectedCity(city);                                       // Step 59: persist it

        tvLocation.setText(city.label());                             // Step 57: header shows the choice
        etCitySearch.setText(city.name);                              // Step 57: keep the name, not the region
        setSearchBarVisible(false);                                   // UI redesign: collapse bar & keyboard

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

            if (selectedCity != null) {                                  // Step 87: never overwrite a user-chosen city
                Log.d("LOCATION", "Ignoring late geocode — city already selected");
                return;
            }

            Log.d("LOCATION", "City: " + city);                          // Step 46

            tvLocation.setText(city != null ? city : "Unknown location"); // Step 47
        });
    }


    /** Step 26 — send request off the main thread. */
    private void fetchCurrentWeather(double lat, double lon) {
        lastLat = lat;                               // Step 48: remember for refresh
        lastLon = lon;
        lastFailedSearch = false;                    // Step 73: weather is the latest action now

        if (!isOnline()) {                                        // Step 74: fail fast, don't wait 10 s
            Log.w(TAG, "Offline — skipping weather request");
            showError("You're offline. Showing the last loaded weather. "
                    + "Tap Retry when you're back online.");      // Step 74
            return;
        }

        WeatherApi api = RetrofitClient.getApi();

        Call<WeatherResponse> call =
                api.getWeather(lat, lon, CURRENT_FIELDS, HOURLY_FIELDS, DAILY_FIELDS, "auto", 7);
        //                                  current ─┘   hourly ─┘     daily ─┘        tz    forecast_days=7
        //  7 days for the weekly list (step 69); the hourly strip slices the first 24 in step 64

        enqueueApiCall(TAG, call, new ApiResponseHandler<WeatherResponse>() {   // Step 89: shared Retrofit wrapper

            @Override
            public void onSuccess(WeatherResponse body) {
                handleSuccess(body);                                           // Step 27
            }

            @Override
            public void onHttpError(int code, ResponseBody errorBody) {
                handleHttpError(code, errorBody);                              // Step 28
            }

            @Override
            public void onNetworkError(Throwable t) {
                handleNetworkFailure(t);                                       // Step 28
            }
        });
    }

    /** Step 89: contract for handling a finished Retrofit call on the main thread. */
    private interface ApiResponseHandler<T> {
        void onSuccess(T body);
        void onHttpError(int code, ResponseBody errorBody);
        void onNetworkError(Throwable t);
    }

    /** Step 89: shared Retrofit enqueue wrapper — logs URL, manages spinner, routes response/failure. */
    private <T> void enqueueApiCall(String logTag, Call<T> call, ApiResponseHandler<T> handler) {

        Log.d(logTag, "Request URL: " + call.request().url());                 // Step 89
        beginRequest();                                                        // Step 71 + 89: spinner + lock buttons

        call.enqueue(new Callback<T>() {

            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                endRequest();                                                  // Step 71 + 89

                if (response.isSuccessful()) {
                    handler.onSuccess(response.body());
                } else {
                    handler.onHttpError(response.code(), response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                endRequest();                                                  // Step 71 + 89
                handler.onNetworkError(t);
            }
        });
    }

    /** Step 71: one more request in flight — show the spinner, lock the buttons. */
    private void beginRequest() {

        activeRequests++;
        setLoading(true);
        hideError();                     // Step 72: a new attempt clears the old message
    }

    /** Step 72: show a user-facing error message. */
    private void showError(String message) {

        tvError.setText(message);        // Step 72
        llError.setVisibility(View.VISIBLE);   // Step 73
    }

    /** Step 72: hide the error banner. */
    private void hideError() {

        llError.setVisibility(View.GONE);       // Step 72-73
    }

    /** Step 74: is there a usable internet connection right now? */
    boolean isOnline() {                                               // Step 86: package-private for test

        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);   // Step 74

        if (cm == null) return true;                        // can't tell: let the request try

        Network network = cm.getActiveNetwork();            // Step 74
        if (network == null) return false;                  // no Wi-Fi, no mobile data

        NetworkCapabilities caps = cm.getNetworkCapabilities(network);   // Step 74
        return caps != null
                && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    /** Step 77: drag down from the very top to refresh, without a new dependency. */
    private void setupPullToRefresh() {

        ScrollView scroll = findViewById(R.id.main);              // Step 77: the root ScrollView

        final float threshold =
                24 * getResources().getDisplayMetrics().density;   // ~24dp drag
        final float[] startY = {0f};                               // where the finger went down
        final boolean[] armed = {false};                           // start only from scrollY == 0

        scroll.setOnTouchListener((v, event) -> {

            switch (event.getActionMasked()) {

                case MotionEvent.ACTION_DOWN:                      // Step 77
                    startY[0] = event.getY();
                    armed[0] = scroll.getScrollY() == 0;
                    break;

                case MotionEvent.ACTION_MOVE:                      // Step 77
                    if (!armed[0] && scroll.getScrollY() == 0) {   // UI redesign: arm even if drag started on child card
                        startY[0] = event.getY();
                        armed[0] = true;
                    }
                    if (armed[0]
                            && scroll.getScrollY() == 0
                            && event.getY() - startY[0] > threshold) {

                        armed[0] = false;                          // one trigger per drag

                        if (activeRequests == 0) {
                            Log.d("REFRESH", "Pull-to-refresh triggered");   // Step 77
                            refreshWeather();
                        }
                    }
                    break;

                case MotionEvent.ACTION_UP:                        // Step 77
                case MotionEvent.ACTION_CANCEL:
                    armed[0] = false;
                    break;
            }

            return false;                                          // let the ScrollView scroll normally
        });
    }

    /** Step 73: repeat whichever request failed last. */
    private void retryLastRequest() {
        Log.d("RETRY", "Retry tapped, failedSearch=" + lastFailedSearch);   // Step 73

        if (lastFailedSearch && lastSearchQuery != null) {
            performSearch(lastSearchQuery);      // Step 73: search again
        } else {
            refreshWeather();                    // Step 73: weather again
        }
    }

    /** Step 71: one request finished — hide the spinner when none are left. */
    private void endRequest() {

        activeRequests = Math.max(0, activeRequests - 1);
        if (activeRequests == 0) {
            setLoading(false);
        }
    }

    /** Step 71: spinner visibility + button state. */
    private void setLoading(boolean loading) {

        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnToggleSearch.setEnabled(!loading);                        // UI redesign: search lens button
        btnSearch.setEnabled(!loading);
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
        logHourly();                          // Step 62
        logDaily();                           // Step 67
        renderCurrentWeather();               // Step 30
        renderHourly();                       // Step 64
        renderDaily();                        // Step 69
    }

    /** Step 69: fill the 7-day list. */
    private void renderDaily() {

        if (currentWeather == null || currentWeather.daily == null
                || currentWeather.daily.size() == 0) {
            dailyAdapter.submitList(null);                         // Step 69: clear the list
            Log.w(TAG, "renderDaily: no usable daily data");
            return;
        }

        WeatherResponse.Daily d = currentWeather.daily;
        int days = Math.min(d.size(), 7);                          // Step 69: at most 7 rows

        List<DailyItem> items = new ArrayList<>();                 // Step 69

        for (int i = 0; i < days; i++) {
            String date = d.time.get(i);
            Double max = d.temperatureMax.get(i);
            Double min = d.temperatureMin.get(i);
            Integer code = d.weatherCode.get(i);
            if (date == null || max == null || min == null || code == null) continue;   // Step 87: skip null element

            items.add(new DailyItem(
                    dayLabel(date, items.size()),                  // "Today" / "Tomorrow" / "Friday"
                    max,                                           // step 70 reads these
                    min,
                    code,
                    items.isEmpty()));                             // Step 69
        }

        // Ensure the daily RecyclerView is visible when we have data
        rvDaily.setVisibility(View.VISIBLE);
        dailyAdapter.submitList(items);                            // Step 69
        Log.d(TAG, "Daily cells: " + items.size());
    }

    /** Step 69: "2026-11-25" -> "Today" / "Tomorrow" / "Friday". */
    private String dayLabel(String isoDate, int index) {

        if (index == 0) return "Today";                            // Step 69
        if (index == 1) return "Tomorrow";                         // Step 69
        if (isoDate == null) return "";                            // Step 87: guard null date

        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(isoDate);
            if (date == null) return isoDate;
            return new SimpleDateFormat("EEEE", Locale.getDefault()).format(date);   // Step 69
        } catch (ParseException e) {
            return isoDate;                                        // unexpected format: show raw
        }
    }

    /** Step 67: prove the daily arrays parsed correctly. */
    private void logDaily() {

        if (currentWeather == null || currentWeather.daily == null) {
            Log.w(TAG, "No daily block in the response");      // Step 67
            return;
        }

        WeatherResponse.Daily d = currentWeather.daily;
        int days = d.size();                                   // Step 66: safe length

        Log.d(TAG, "----- DAILY (" + days + " days) -----");   // Step 67

        for (int i = 0; i < Math.min(days, 7); i++) {
            Log.d(TAG, d.time.get(i)
                    + "  max=" + d.temperatureMax.get(i)
                    + "  min=" + d.temperatureMin.get(i)
                    + "  code=" + d.weatherCode.get(i));       // Step 67
        }
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
        if (currentWeather.current != null && currentWeather.current.time != null
                && currentWeather.current.time.length() >= 13) {   // Step 87: guard short timestamp
            String nowPrefix = currentWeather.current.time.substring(0, 13);   // "2026-11-25T10"
            for (int i = 0; i < hours; i++) {
                String slotTime = h.time.get(i);
                if (slotTime != null && slotTime.startsWith(nowPrefix)) {   // Step 87: guard null slot
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
            String slotTime = h.time.get(i);
            Double temp = h.temperature.get(i);
            Integer code = h.weatherCode.get(i);
            if (slotTime == null || temp == null || code == null) continue;   // Step 87: skip null element

            boolean isDay = h.isDay != null && i < h.isDay.size() && h.isDay.get(i) != null   // Step 65 + 87
                    ? h.isDay.get(i) == 1
                    : dayFallback;

            boolean isNow = items.isEmpty();                       // Step 64 + 87: first valid cell is "NOW"
            items.add(new HourlyItem(
                    hourLabel(slotTime, isNow),                    // "NOW" / "10 PM"
                    temp,
                    code,
                    isDay,
                    isNow));                                       // Step 64

            if (isNow) {                                           // Step 65: verify the icon choice
                Log.d(TAG, "First hour icon: " + getResources().getResourceEntryName(
                        WeatherCodeIconMapper.iconFor(code, isDay)));
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

        int icon = WeatherCodeIconMapper.iconFor(c.weatherCode, c.isDay == 1);   // Step 40 (Step 88: temporary storm test removed)

        ivCurrentWeather.setImageResource(icon);        // Step 40: big card icon
        ivHeaderWeather.setImageResource(icon);         // Step 40: header icon

        // Step 70: today's high / low straight from daily[0]
        if (currentWeather.daily != null && currentWeather.daily.size() > 0) {
            double high = currentWeather.daily.temperatureMax.get(0);
            double low = currentWeather.daily.temperatureMin.get(0);

            tvHighLow.setText("H: " + Math.round(high) + "°   L: "     // Step 70
                    + Math.round(low) + "°");
        } else {
            tvHighLow.setText("H: --   L: --");                      // Step 70: no daily data
        }

        applyTheme(c.isDay == 1);                                    // Step 79: day / night palette

        describeForTalkBack(c);                                      // Step 80
    }

    /** Step 80: one spoken sentence for the whole card instead of six loose numbers. */
    private void describeForTalkBack(WeatherResponse.CurrentWeather c) {

        StringBuilder summary = new StringBuilder();                 // Step 80

        summary.append(Math.round(c.temperature)).append(" degrees, ")
                .append(WeatherCodeMapper.describe(c.weatherCode))
                .append(", feels like ")
                .append(Math.round(c.feelsLike)).append(" degrees");

        if (currentWeather.daily != null && currentWeather.daily.size() > 0) {
            summary.append(", high ")
                    .append(Math.round(currentWeather.daily.temperatureMax.get(0)))
                    .append(", low ")
                    .append(Math.round(currentWeather.daily.temperatureMin.get(0)));
        }

        View card = findViewById(R.id.cardMain);                     // Step 80
        card.setContentDescription(summary.toString());

        String iconText = WeatherCodeMapper.describe(c.weatherCode)
                + (c.isDay == 1 ? ", day" : ", night");              // Step 80

        ivCurrentWeather.setContentDescription(iconText);            // Step 80
        ivHeaderWeather.setContentDescription(iconText);             // Step 80
    }

    /** Step 79: light palette by day, dark palette at night (driven by API is_day). */
    private void applyTheme(boolean isDay) {

        nightTheme = !isDay;                                         // Step 79

        int pageBg = isDay ? 0xFFF4F7FC : 0xFF0F1724;                // Step 79
        int cardTint = isDay ? 0xFFFFFFFF : 0xFF1B2432;              // Step 79
        int heading = isDay ? 0xFF172B4D : 0xFFE6EDF7;               // Step 79
        int value = isDay ? 0xFF172B4D : 0xFFF2F6FC;                 // Step 79

        findViewById(R.id.main).setBackgroundColor(pageBg);          // Step 79

        int[] cardIds = {R.id.cardHumidity, R.id.cardWind,            // Step 79
                R.id.cardPrecipitation, R.id.rvDaily, R.id.llSearchResults,
                R.id.btnToggleSearch};                               // UI redesign: pill button follows theme

        for (int id : cardIds) {

            View view = findViewById(id);
            if (view == null) continue;

            Drawable bg = view.getBackground();
            if (bg == null) continue;

            bg = bg.mutate();                                        // Step 79: own copy per view
            bg.setTint(cardTint);
            view.setBackground(bg);
        }

        ((TextView) findViewById(R.id.tvDetailsHeading)).setTextColor(heading);   // Step 79
        ((TextView) findViewById(R.id.tvHourlyHeading)).setTextColor(heading);
        ((TextView) findViewById(R.id.tvDailyHeading)).setTextColor(heading);

        btnToggleSearch.setColorFilter(value);                       // UI redesign: search lens tint
        tvLocation.setTextColor(value);                              // Step 79
        tvHumidity.setTextColor(value);
        tvWindSpeed.setTextColor(value);
        tvPrecipitation.setTextColor(value);
        tvError.setTextColor(isDay ? 0xFFB3261E : 0xFFFFB4AB);       // Step 79

        // Step 87: keep search box and error banner readable against night/day backgrounds
        int searchHint = isDay ? 0xFF718096 : 0xFF94A3B8;
        etCitySearch.setTextColor(value);
        etCitySearch.setHintTextColor(searchHint);
        searchLayout.setDefaultHintTextColor(ColorStateList.valueOf(searchHint));
        searchLayout.setBoxStrokeColor(isDay ? 0xFF2F6FED : 0xFF6BA6FF);

        Drawable errorBg = llError.getBackground();
        if (errorBg != null) {
            errorBg = errorBg.mutate();
            errorBg.setTint(isDay ? 0xFFFDECEA : 0xFF3B1D22);        // Step 87: dark red card at night
            llError.setBackground(errorBg);
        }

        hourlyAdapter.setNightMode(!isDay);                          // Step 79
        dailyAdapter.setNightMode(!isDay);

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(isDay);              // Step 79: dark icons in daylight
        controller.setAppearanceLightNavigationBars(isDay);
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

        // Step 72: tell the user, not just Logcat
        showError("Weather request failed (HTTP " + code + "). Pull down to refresh.");
    }

    /** Step 28 — never reached the server. */
    private void handleNetworkFailure(Throwable t) {

        if (t instanceof UnknownHostException) {
            Log.e(TAG, "No internet or DNS failure", t);
            showError("No internet connection. Turn on Wi-Fi or mobile data, then pull down to refresh.");  // Step 72
        } else if (t instanceof SocketTimeoutException) {
            Log.e(TAG, "Request timed out", t);
            showError("The weather service is slow right now. Pull down to refresh.");        // Step 72
        } else if (t instanceof IOException) {
            Log.e(TAG, "I/O problem during request", t);
            showError("Connection problem. Pull down to refresh.");                           // Step 72
        } else {
            Log.e(TAG, "Unexpected failure", t);
            showError("Something went wrong while loading the weather.");                         // Step 72
        }
    }
}