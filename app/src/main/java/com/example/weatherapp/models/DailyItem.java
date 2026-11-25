package com.example.weatherapp.models;

/** Step 68: one day of forecast, flattened from WeatherResponse.Daily. */
public class DailyItem {

    public String label;        // "Today", "Tomorrow", "Wed"
    public double high;         // °C
    public double low;          // °C
    public int weatherCode;     // WMO code -> WeatherCodeIconMapper
    public boolean isToday;     // bolds the first row

    public DailyItem(String label, double high, double low, int weatherCode, boolean isToday) {
        this.label = label;
        this.high = high;
        this.low = low;
        this.weatherCode = weatherCode;
        this.isToday = isToday;
    }
}
