package com.example.weatherapp.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//Retrofit is a type-safe HTTP and REST client library for Android and Java/Kotlin developed by Square. It simplifies network operations by letting you define API endpoints using simple Java or Kotlin interfaces and annotations (like @GET or @POST)

public class RetrofitClient {

    private static final String BASE_URL =
            "https://api.open-meteo.com/";

    private static Retrofit retrofit;

    public static WeatherApi getApi() {

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(
                            GsonConverterFactory.create()
                    )
                    .build();
        }

        return retrofit.create(WeatherApi.class);
    }
}
