package com.example.weatherapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.weatherapp.models.GeocodingResponse;
import com.google.gson.Gson;

import org.junit.Test;

/**
 * Step 84: proves the city-search payload parses and that the label used by the
 * result rows (step 56) survives missing region/country fields.
 */
public class GeocodingResponseTest {

    private static final String SEARCH_JSON = "{\n"
            + "  \"results\": [\n"
            + "    {\"id\":1179400,\"name\":\"Faisalabad\",\"latitude\":31.41554,"
            + "     \"longitude\":73.08969,\"country\":\"Pakistan\",\"admin1\":\"Punjab\"},\n"
            + "    {\"id\":1482732,\"name\":\"Faisalabad\",\"latitude\":34.32913,"
            + "     \"longitude\":72.28484,\"country\":\"Pakistan\",\"admin1\":\"Khyber Pakhtunkhwa\"}\n"
            + "  ],\n"
            + "  \"generationtime_ms\": 0.107\n"
            + "}";

    private GeocodingResponse parse(String json) {
        return new Gson().fromJson(json, GeocodingResponse.class);
    }

    @Test
    public void parsesTwoMatches() {

        GeocodingResponse response = parse(SEARCH_JSON);

        assertNotNull(response);
        assertNotNull(response.results);
        assertEquals(2, response.results.size());

        GeocodingResponse.Result first = response.results.get(0);

        assertEquals("Faisalabad", first.name);
        assertEquals(31.41554, first.latitude, 0.00001);
        assertEquals(73.08969, first.longitude, 0.00001);
        assertEquals("Pakistan", first.country);
        assertEquals("Punjab", first.admin1);
    }

    @Test
    public void labelCombinesNameRegionCountry() {

        GeocodingResponse.Result r = parse(SEARCH_JSON).results.get(0);

        assertEquals("Faisalabad, Punjab, Pakistan", r.label());
    }

    @Test
    public void labelSkipsMissingParts() {

        GeocodingResponse.Result r = new GeocodingResponse.Result();
        r.name = "Faisalabad";
        r.country = "Pakistan";                  // region null

        assertEquals("Faisalabad, Pakistan", r.label());

        GeocodingResponse.Result onlyName = new GeocodingResponse.Result();
        onlyName.name = "Faisalabad";

        assertEquals("Faisalabad", onlyName.label());
    }

    @Test
    public void labelNeverReturnsEmptyString() {

        GeocodingResponse.Result empty = new GeocodingResponse.Result();

        assertEquals("Unknown place", empty.label());   // step 56 fallback
    }

    @Test
    public void unknownCityReturnsNoResultsArray() {

        GeocodingResponse response = parse("{}");       // API sends this for gibberish

        assertNotNull(response);
        assertNull(response.results);                   // step 75 shows the empty state
    }

    @Test
    public void emptyResultsArrayIsTreatedAsNoMatch() {

        GeocodingResponse response = parse("{\"results\":[]}");

        assertNotNull(response.results);
        assertTrue(response.results.isEmpty());         // performSearch() checks isEmpty()
    }
}
