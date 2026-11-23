package com.example.weatherapp.models;

import com.google.gson.annotations.SerializedName;

/** Open-Meteo error envelope: {"error":true,"reason":"..."} */
public class ApiError {

    @SerializedName("error")
    public boolean error;

    @SerializedName("reason")
    public String reason;
}