package com.example.weatherapp.util;

import com.example.weatherapp.R;

/** Step 36: WMO weather code + day/night -> drawable icon. */
public final class WeatherCodeIconMapper {

    private WeatherCodeIconMapper() { }             // utility class

    public static int iconFor(int code, boolean isDay) {

        if (code == 0) {                            // Step 36: clear sky
            return isDay ? R.drawable.day : R.drawable.night;
        }

        return R.drawable.cloudy;                   // temporary fallback (steps 37-40)
    }
}