package com.example.weatherapp.api;

import com.example.weatherapp.models.WeatherResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi {

    @GET("v1/forecast")
//    tells Retrofit which API endpoint to access.
//@Query sends location and weather requirements.
//Call<WeatherResponse> receives the API's typed WeatherResponse model.
    Call<WeatherResponse> getWeather(
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("current") String current,
            @Query("hourly") String hourly,
            @Query("daily") String daily,
            @Query("timezone") String timezone,
            @Query("forecast_days") int forecastDays
    );
}
