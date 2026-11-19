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


    }
}
