package com.example.weatherapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.example.weatherapp.models.WeatherResponse;
import com.google.gson.Gson;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Step 82: proves the hourly block parses and that hourly arrays stay aligned.
 * The API returns four *parallel* arrays, so index i must mean the same hour in all of them.
 */
public class HourlyForecastTest {

    private static final String HOURLY_JSON = "{\n"
            + "  \"timezone\": \"Asia/Karachi\",\n"
            + "  \"hourly\": {\n"
            + "    \"time\": [\"2026-11-25T00:00\", \"2026-11-25T01:00\", \"2026-11-25T02:00\"],\n"
            + "    \"temperature_2m\": [14.2, 13.8, 13.5],\n"
            + "    \"weather_code\": [1, 2, 3],\n"
            + "    \"is_day\": [0, 0, 0]\n"
            + "  }\n"
            + "}";

    private WeatherResponse parse(String json) {
        return new Gson().fromJson(json, WeatherResponse.class);
    }

    @Test
    public void parsesHourlyArrays() {

        WeatherResponse weather = parse(HOURLY_JSON);

        assertNotNull(weather);
        assertNotNull("hourly block must be present", weather.hourly);

        WeatherResponse.Hourly h = weather.hourly;

        assertEquals(3, h.size());
        assertEquals(3, h.time.size());

        assertEquals("2026-11-25T00:00", h.time.get(0));
        assertEquals(14.2, h.temperature.get(0), 0.001);
        assertEquals(1, (int) h.weatherCode.get(0));
        assertEquals(0, (int) h.isDay.get(0));

        assertEquals("2026-11-25T02:00", h.time.get(2));
        assertEquals(13.5, h.temperature.get(2), 0.001);
        assertEquals(3, (int) h.weatherCode.get(2));
    }

    @Test
    public void sizeUsesTheShortestRequiredArray() {

        WeatherResponse.Hourly h = new WeatherResponse.Hourly();

        h.time = threeTimes();                        // 3 entries
        h.temperature = threeTemperatures(2);         // only 2
        h.weatherCode = threeCodes();                 // 3
        h.isDay = null;                               // optional (step 65)

        assertEquals("must not return more than the shortest array", 2, h.size());
    }

    @Test
    public void sizeIsZeroWhenEssentialArrayMissing() {

        WeatherResponse.Hourly h = new WeatherResponse.Hourly();

        h.time = threeTimes();
        h.temperature = null;                         // essential: no temperatures
        h.weatherCode = threeCodes();
        h.isDay = threeIsDay();

        assertEquals(0, h.size());                    // UI falls back to "no hourly data"
    }

    @Test
    public void missingHourlyBlockParsesAsNull() {

        WeatherResponse weather = parse("{\"timezone\":\"UTC\"}");

        assertNotNull(weather);
        assertNull(weather.hourly);                   // renderHourly() guards this
    }

    // ---- helpers ----

    private List<String> threeTimes() {
        List<String> list = new ArrayList<>();
        list.add("2026-11-25T00:00");
        list.add("2026-11-25T01:00");
        list.add("2026-11-25T02:00");
        return list;
    }

    private List<Double> threeTemperatures(int count) {
        List<Double> list = new ArrayList<>();
        for (int i = 0; i < count; i++) list.add(14.0 - i);
        return list;
    }

    private List<Integer> threeCodes() {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        return list;
    }

    private List<Integer> threeIsDay() {
        List<Integer> list = new ArrayList<>();
        list.add(0);
        list.add(0);
        list.add(1);
        return list;
    }
}
