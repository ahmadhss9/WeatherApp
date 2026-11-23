package com.example.weatherapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.weatherapp.models.WeatherResponse;
import com.example.weatherapp.api.RetrofitClient;



public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        RetrofitClient.getApi().getWeather(
                31.418,
                73.079,
                "temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,weather_code,wind_speed_10m",
                "",
                "",
                "auto",
                1
        ).enqueue(new retrofit2.Callback<WeatherResponse>() {

            @Override
            public void onResponse(
                    retrofit2.Call<WeatherResponse> call,
                    retrofit2.Response<WeatherResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse weather = response.body();

                    android.util.Log.d(
                            "WEATHER_TEST",
                            "Temperature: " + weather.current.temperature
                    );
                } else {
                    android.util.Log.e(
                            "WEATHER_TEST",
                            "API error: " + response.code()
                    );
                }
            }

            @Override
            public void onFailure(
                    retrofit2.Call<WeatherResponse> call,
                    Throwable t) {

                android.util.Log.e(
                        "WEATHER_TEST",
                        "Request failed: " + t.getMessage()
                );
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}