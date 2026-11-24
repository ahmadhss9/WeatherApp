package com.example.weatherapp.utils;

import com.example.weatherapp.R;

public class WeatherCodeIconMapper {

    private WeatherCodeIconMapper() { }

    public static int iconFor(int code, boolean isDay) {

        switch (code) {

            case 0:                                         // Step 36: clear sky
                return isDay ? R.drawable.day : R.drawable.night;

            case 1:                                         // Step 37: mainly clear
                return isDay ? R.drawable.cloudy_day_1 : R.drawable.cloudy_night_1;

            case 2:                                         // Step 37: partly cloudy
                return isDay ? R.drawable.cloudy_day_2 : R.drawable.cloudy_night_2;

            case 3:                                         // Step 37: overcast
                return isDay ? R.drawable.cloudy_day_3 : R.drawable.cloudy_night_3;

            // ---- Step 38: rain (same icon day and night) ----
            case 51: return R.drawable.rainy_1;   // light drizzle
            case 53: return R.drawable.rainy_2;   // drizzle
            case 55: return R.drawable.rainy_3;   // dense drizzle
            case 56: return R.drawable.rainy_3;   // freezing drizzle
            case 57: return R.drawable.rainy_4;   // dense freezing drizzle
            case 61: return R.drawable.rainy_4;   // light rain
            case 63: return R.drawable.rainy_5;   // rain
            case 65: return R.drawable.rainy_6;   // heavy rain
            case 66: return R.drawable.rainy_5;   // freezing rain
            case 67: return R.drawable.rainy_6;   // heavy freezing rain
            case 80: return R.drawable.rainy_3;   // light showers
            case 81: return R.drawable.rainy_5;   // showers
            case 82: return R.drawable.rainy_7;   // violent showers

            case 45:                                        // Step 37: fog
            case 48:
                return R.drawable.cloudy;

            default:                                        // rain/snow/storm: steps 38-39
                return R.drawable.cloudy;
        }
    }
}
