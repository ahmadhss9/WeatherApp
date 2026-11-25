package com.example.weatherapp.api;

import com.example.weatherapp.models.GeocodingResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/** Step 51: Open-Meteo geocoding — city name -> coordinates. */
public interface GeocodingApi {

    @GET("v1/search")
    Call<GeocodingResponse> searchCities(
            @Query("name") String name,        // e.g. "Lahore"
            @Query("count") int count,         // how many matches
            @Query("language") String language,// "en"
            @Query("format") String format     // "json"
    );
}