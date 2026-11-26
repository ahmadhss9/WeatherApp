package com.example.weatherapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.weatherapp.models.WeatherResponse;
import com.google.gson.Gson;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Step 83: proves the daily block parses, that the *_max / *_min keys are not swapped,
 * and that Daily.size() can never point past the end of an array.
 */
public class DailyForecastTest {

    private static final String DAILY_JSON = "{\n"
            + "  \"timezone\": \"Asia/Karachi\",\n"
            + "  \"daily\": {\n"
            + "    \"time\": [\"2026-11-25\", \"2026-11-26\", \"2026-11-27\"],\n"
            + "    \"weather_code\": [1, 3, 61],\n"
            + "    \"temperature_2m_max\": [24.1, 25.6, 21.4],\n"
            + "    \"temperature_2m_min\": [12.3, 12.9, 11.0]\n"
            + "  }\n"
            + "}";

    private WeatherResponse parse(String json) {
        return new Gson().fromJson(json, WeatherResponse.class);
    }

    @Test
    public void parsesDailyArrays() {

        WeatherResponse weather = parse(DAILY_JSON);

        assertNotNull(weather);
        assertNotNull("daily block must be present", weather.daily);

        WeatherResponse.Daily d = weather.daily;

        assertEquals(3, d.size());
        assertEquals("2026-11-25", d.time.get(0));
        assertEquals(24.1, d.temperatureMax.get(0), 0.001);
        assertEquals(12.3, d.temperatureMin.get(0), 0.001);
        assertEquals(61, (int) d.weatherCode.get(2));
    }

    @Test
    public void highIsAlwaysAboveLow() {

        WeatherResponse.Daily d = parse(DAILY_JSON).daily;

        for (int i = 0; i < d.size(); i++) {
            double high = d.temperatureMax.get(i);
            double low = d.temperatureMin.get(i);

            // If this ever fails, the @SerializedName of max/min got crossed
            assertTrue("day " + i + ": high " + high + " must be above low " + low,
                    high > low);
        }
    }

    @Test
    public void sizeUsesTheShortestArray() {

        WeatherResponse.Daily d = new WeatherResponse.Daily();

        d.time = days(7);                       // 7 dates
        d.weatherCode = codes(7);               // 7 codes
        d.temperatureMax = temps(7, 20.0);      // 7 highs
        d.temperatureMin = temps(5, 10.0);      // only 5 lows

        assertEquals(5, d.size());
    }

    @Test
    public void sizeIsZeroWhenMaxOrMinMissing() {

        WeatherResponse.Daily d = new WeatherResponse.Daily();

        d.time = days(7);
        d.weatherCode = codes(7);
        d.temperatureMax = null;                // essential
        d.temperatureMin = temps(7, 10.0);

        assertEquals(0, d.size());
    }

    @Test
    public void missingDailyBlockParsesAsNull() {

        WeatherResponse weather = parse("{\"timezone\":\"UTC\"}");

        assertNotNull(weather);
        assertNull(weather.daily);              // renderDaily() and step 70 guard this
    }

    // ---- helpers ----

    private List<String> days(int count) {
        List<String> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            list.add(String.format("2026-11-%02d", i));
        }
        return list;
    }

    private List<Integer> codes(int count) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < count; i++) list.add(1);
        return list;
    }

    private List<Double> temps(int count, double base) {
        List<Double> list = new ArrayList<>();
        for (int i = 0; i < count; i++) list.add(base + i);
        return list;
    }
}
