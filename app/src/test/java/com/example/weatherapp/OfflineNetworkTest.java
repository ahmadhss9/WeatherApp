package com.example.weatherapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.example.weatherapp.api.GeocodingApi;
import com.example.weatherapp.api.IpLocationApi;
import com.example.weatherapp.api.WeatherApi;
import com.example.weatherapp.models.ApiError;
import com.google.gson.Gson;

import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Step 86: proves Retrofit API calls fail cleanly with IOException when unreachable
 * and that API error payloads parse safely on the JVM (no real internet needed).
 */
public class OfflineNetworkTest {

    // ---- Step 86: unreachable local Retrofit builder (nothing listens on this port) ----
    private Retrofit unreachableRetrofit() throws IOException {
        int freePort;
        try (ServerSocket socket = new ServerSocket(0)) {
            freePort = socket.getLocalPort();               // Step 86: reserve & close immediately
        }

        return new Retrofit.Builder()
                .baseUrl("http://127.0.0.1:" + freePort + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Test
    public void weatherRequestFailsWithIoExceptionWhenUnreachable() throws IOException {

        WeatherApi api = unreachableRetrofit().create(WeatherApi.class);

        try {
            api.getWeather(31.418, 73.079, "temperature_2m",
                    "temperature_2m", "weather_code", "auto", 7).execute();
            fail("expected IOException when endpoint is unreachable");
        } catch (IOException e) {
            assertNotNull(e.getMessage());                  // Step 86: handled by handleNetworkFailure()
        }
    }

    @Test
    public void geocodingRequestFailsWithIoExceptionWhenUnreachable() throws IOException {

        GeocodingApi api = unreachableRetrofit().create(GeocodingApi.class);

        try {
            api.searchCities("London", 5, "en", "json").execute();
            fail("expected IOException when geocoding endpoint is unreachable");
        } catch (IOException e) {
            assertNotNull(e.getMessage());                  // Step 86: handled by performSearch onFailure()
        }
    }

    @Test
    public void ipLocationRequestFailsWithIoExceptionWhenUnreachable() throws IOException {

        IpLocationApi api = unreachableRetrofit().create(IpLocationApi.class);

        try {
            api.getLocation().execute();
            fail("expected IOException when IP location endpoint is unreachable");
        } catch (IOException e) {
            assertNotNull(e.getMessage());                  // Step 86: triggers default city fallback
        }
    }

    @Test
    public void apiErrorEnvelopeParsesReason() {

        String json = "{\"error\":true,\"reason\":\"Cannot initialize WeatherVariable\"}";
        ApiError parsed = new Gson().fromJson(json, ApiError.class);

        assertNotNull(parsed);
        assertTrue(parsed.error);
        assertEquals("Cannot initialize WeatherVariable", parsed.reason);
    }
}
