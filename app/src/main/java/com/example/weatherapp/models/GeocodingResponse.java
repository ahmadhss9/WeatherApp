package com.example.weatherapp.models;

import java.util.List;

/** Step 51-52: response of the geocoding search endpoint. */
public class GeocodingResponse {

    public List<Result> results;        // null when nothing matched

    /** Step 52: one matched place (fields added there). */
    public static class Result { }
}