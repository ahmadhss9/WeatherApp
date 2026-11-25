package com.example.weatherapp.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/** Steps 27-61: the whole /v1/forecast answer (current + hourly). */
public class WeatherResponse {

    @SerializedName("latitude")
    public double latitude;

    @SerializedName("longitude")
    public double longitude;

    @SerializedName("timezone")
    public String timezone;

    @SerializedName("current")
    public CurrentWeather current;

    @SerializedName("hourly")                       // Step 61
    public Hourly hourly;

    public static class CurrentWeather {

        @SerializedName("time")
        public String time;

        @SerializedName("temperature_2m")
        public double temperature;

        @SerializedName("relative_humidity_2m")
        public int humidity;

        @SerializedName("apparent_temperature")
        public double feelsLike;

        @SerializedName("is_day")
        public int isDay;

        @SerializedName("precipitation")
        public double precipitation;

        @SerializedName("weather_code")
        public int weatherCode;

        @SerializedName("wind_speed_10m")
        public double windSpeed;
    }

    /**
     * Step 61: hourly forecast. Open-Meteo returns parallel arrays —
     * time[0] belongs to temperature[0], weatherCode[0], isDay[0], and so on.
     */
    public static class Hourly {

        @SerializedName("time")
        public List<String> time;                   // "2026-11-25T00:00", "2026-11-25T01:00", ...

        @SerializedName("temperature_2m")
        public List<Double> temperature;             // °C

        @SerializedName("weather_code")
        public List<Integer> weatherCode;            // WMO code, mapper from step 35

        @SerializedName("is_day")
        public List<Integer> isDay;                  // 1 = day, 0 = night, per hour

        /** Step 61-65: how many complete hours we can safely display (0 = unusable). */
        public int size() {
            if (time == null || temperature == null || weatherCode == null) {
                return 0;                            // partial block -> show nothing
            }

            int n = time.size();
            n = Math.min(n, temperature.size());
            n = Math.min(n, weatherCode.size());

            // Step 65: is_day is optional — hours without it fall back to current.isDay
            if (isDay != null) n = Math.min(n, isDay.size());
            return n;
        }
    }
}
