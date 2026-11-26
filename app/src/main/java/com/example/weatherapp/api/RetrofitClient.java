package com.example.weatherapp.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//Retrofit is a type-safe HTTP and REST client library for Android and Java/Kotlin developed by Square. It simplifies network operations by letting you define API endpoints using simple Java or Kotlin interfaces and annotations (like @GET or @POST)

public class RetrofitClient {

    private static final String BASE_URL =
            "https://api.open-meteo.com/";
    private static final String GEOCODING_BASE_URL =     // Step 51: city search host
            "https://geocoding-api.open-meteo.com/";

    private static final String IP_BASE_URL =            // Step 76: IP location host
            "https://ipwho.is/";

    private static final GsonConverterFactory GSON_FACTORY =   // Step 89: shared converter
            GsonConverterFactory.create();

    private static Retrofit retrofit;
    private static Retrofit geocodingRetrofit;           // Step 51
    private static Retrofit ipRetrofit;                  // Step 76

    /** Step 89: shared Retrofit builder — avoids repeating baseUrl + Gson setup. */
    private static Retrofit buildClient(String baseUrl) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GSON_FACTORY)
                .build();
    }

    public static WeatherApi getApi() {

        if (retrofit == null) {
            retrofit = buildClient(BASE_URL);            // Step 89
        }

        return retrofit.create(WeatherApi.class);
    }

    /** Step 51: separate client because the base URL is different. */
    public static GeocodingApi getGeocodingApi() {

        if (geocodingRetrofit == null) {
            geocodingRetrofit = buildClient(GEOCODING_BASE_URL);   // Step 89
        }

        return geocodingRetrofit.create(GeocodingApi.class);
    }

    /** Step 76: third client — IP based approximate location. */
    public static IpLocationApi getIpLocationApi() {

        if (ipRetrofit == null) {
            ipRetrofit = buildClient(IP_BASE_URL);       // Step 89
        }

        return ipRetrofit.create(IpLocationApi.class);
    }
}
