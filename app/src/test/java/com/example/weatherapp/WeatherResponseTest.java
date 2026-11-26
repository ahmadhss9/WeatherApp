package com.example.weatherapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.example.weatherapp.models.WeatherResponse;
import com.google.gson.Gson;

import org.junit.Test;

/**
 * Step 81: proves a real "current weather" JSON payload maps onto our model.
 * Runs on the JVM — no emulator, no network.
 */
public class WeatherResponseTest {

    // Trimmed copy of a real Open-Meteo answer (Faisalabad, Nov 2026)
    private static final String CURRENT_JSON = "{\n"
            + "  \"latitude\": 31.3884,\n"
            + "  \"longitude\": 73.03103,\n"
            + "  \"timezone\": \"Asia/Karachi\",\n"
            + "  \"current\": {\n"
            + "    \"time\": \"2026-11-25T06:30\",\n"
            + "    \"interval\": 900,\n"
            + "    \"temperature_2m\": 12.5,\n"
            + "    \"relative_humidity_2m\": 81,\n"
            + "    \"apparent_temperature\": 11.4,\n"
            + "    \"is_day\": 0,\n"
            + "    \"precipitation\": 0.0,\n"
            + "    \"weather_code\": 1,\n"
            + "    \"wind_speed_10m\": 3.9\n"
            + "  }\n"
            + "}";

    private WeatherResponse parse(String json) {
        return new Gson().fromJson(json, WeatherResponse.class);
    }

    @Test
    public void parsesCurrentWeatherFields() {

        WeatherResponse weather = parse(CURRENT_JSON);

        assertNotNull("response must parse", weather);
        assertNotNull("current block must be present", weather.current);

        WeatherResponse.CurrentWeather c = weather.current;

        assertEquals("Asia/Karachi", weather.timezone);

        assertEquals("2026-11-25T06:30", c.time);
        assertEquals(12.5, c.temperature, 0.001);
        assertEquals(81, c.humidity);
        assertEquals(11.4, c.feelsLike, 0.001);
        assertEquals(0, c.isDay);
        assertEquals(0.0, c.precipitation, 0.001);
        assertEquals(1, c.weatherCode);
        assertEquals(3.9, c.windSpeed, 0.001);
    }

    @Test
    public void missingCurrentBlockDoesNotCrashParsing() {

        WeatherResponse weather = parse("{\"timezone\":\"UTC\"}");   // no "current"

        assertNotNull(weather);
        assertNull(weather.current);                          // guards in MainActivity handle this
    }
}
