package com.example.weatherapp.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/** Step 51-52: response of the geocoding search endpoint. */
public class GeocodingResponse {

    // Step 52: null when nothing matched (the API returns {} in that case)
    @SerializedName("results")
    public List<Result> results;

    /** Step 52: one matched place. */
    public static class Result {

        @SerializedName("id")
        public long id;

        @SerializedName("name")
        public String name;             // "Faisalabad"

        @SerializedName("latitude")
        public double latitude;         // 31.41554

        @SerializedName("longitude")
        public double longitude;        // 73.08969

        @SerializedName("country")
        public String country;          // "Pakistan"

        @SerializedName("admin1")
        public String admin1;           // "Punjab"

        /** Step 52-56: one-line label for the results list. */
        public String label() {
            StringBuilder sb = new StringBuilder();
            if (name != null) sb.append(name);
            if (admin1 != null && !admin1.isEmpty()) sb.append(", ").append(admin1);
            if (country != null && !country.isEmpty()) sb.append(", ").append(country);
            return sb.length() == 0 ? "Unknown place" : sb.toString();
        }
    }
}