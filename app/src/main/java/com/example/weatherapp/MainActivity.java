package com.example.weatherapp;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.weatherapp.api.RetrofitClient;
import com.example.weatherapp.api.WeatherApi;
import com.example.weatherapp.models.ApiError;
import com.example.weatherapp.models.WeatherResponse;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import okhttp3.ResponseBody;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "WEATHER_TEST";

    private static final String CURRENT_FIELDS =
            "temperature_2m,relative_humidity_2m,apparent_temperature,"
                    + "is_day,precipitation,weather_code,wind_speed_10m";

    private WeatherResponse currentWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fetchCurrentWeather(31.418, 73.079);
    }

    private void fetchCurrentWeather(double lat, double lon) {

        WeatherApi api = RetrofitClient.getApi();
        Call<WeatherResponse> call =
                api.getWeather(lat, lon, CURRENT_FIELDS, "", "", "auto", 1);

        Log.d(TAG, "Request URL: " + call.request().url());

        call.enqueue(new Callback<WeatherResponse>() {

            @Override
            public void onResponse(Call<WeatherResponse> call,
                                   Response<WeatherResponse> response) {

                if (response.isSuccessful()) {
                    handleSuccess(response.body());
                } else {
                    handleHttpError(response.code(), response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                handleNetworkFailure(t);
            }
        });
    }

    /** Success path: HTTP 2xx with a body that parsed. */
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
        logWeather(currentWeather.current);
    }

    /** Step 29: one readable dump of everything the API returned. */
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

    /** Server answered, but refused the request. */
    private void handleHttpError(int code, ResponseBody errorBody) {

        String body = "no error body";

        if (errorBody != null) {
            try {
                body = errorBody.string();
            } catch (IOException e) {
                body = "<unreadable>";
            }
        }

        Log.e(TAG, "HTTP " + code + " — " + body);

        ApiError parsed = null;
        try {
            parsed = new Gson().fromJson(body, ApiError.class);
        } catch (JsonSyntaxException ignored) {
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

    /** Never reached the server. */
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