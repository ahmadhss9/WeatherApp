package com.example.weatherapp.models;

/** Step 63: one hour of forecast, flattened from WeatherResponse.Hourly. */
public class HourlyItem {

    public String label;        // "NOW" or "3 PM"
    public double temperature;  // °C
    public int weatherCode;     // WMO code -> WeatherCodeIconMapper
    public boolean isDay;       // drives day/night icon
    public boolean isNow;       // highlights the first cell

    public HourlyItem(String label, double temperature, int weatherCode,
                      boolean isDay, boolean isNow) {
        this.label = label;
        this.temperature = temperature;
        this.weatherCode = weatherCode;
        this.isDay = isDay;
        this.isNow = isNow;
    }
}
