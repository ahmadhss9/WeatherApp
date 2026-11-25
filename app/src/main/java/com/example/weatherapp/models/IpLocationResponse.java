package com.example.weatherapp.models;

import com.google.gson.annotations.SerializedName;

/** Step 76: answer from https://ipwho.is (approximate location from the public IP). */
public class IpLocationResponse {

    @SerializedName("success")
    public boolean success;

    @SerializedName("city")
    public String city;

    @SerializedName("region")
    public String region;

    @SerializedName("country")
    public String country;

    @SerializedName("latitude")
    public double latitude;

    @SerializedName("longitude")
    public double longitude;
}
