package com.example.weatherapp.models;

import com.google.gson.annotations.SerializedName;

public class WeatherResponse {

    @SerializedName("latitude")
    public double latitude;

    @SerializedName("longitude")
    public double longitude;

    @SerializedName("timezone")
    public String timezone;

    @SerializedName("current")
    public CurrentWeather current;

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
}
