package com.example.weatherapp.api;

import com.example.weatherapp.models.IpLocationResponse;

import retrofit2.Call;
import retrofit2.http.GET;

/** Step 76: approximate location from the public IP address (no key needed). */
public interface IpLocationApi {

    @GET("/")                                   // Step 76: https://ipwho.is/
    Call<IpLocationResponse> getLocation();
}
