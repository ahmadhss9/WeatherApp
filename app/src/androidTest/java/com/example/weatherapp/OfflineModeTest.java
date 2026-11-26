package com.example.weatherapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import androidx.core.content.ContextCompat;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Step 86: tests offline detection and UI behavior when there is no internet.
 *
 * Run on a device/emulator:  gradlew connectedDebugAndroidTest
 * Airplane-mode / mid-request drops are documented in docs/testing/offline-scenarios.md.
 */
@RunWith(AndroidJUnit4.class)
public class OfflineModeTest {

    private static final String PREFS = "weather_prefs";
    private static final String KEY_CITY_LABEL = "city_label";
    private static final String KEY_CITY_LAT = "city_lat";
    private static final String KEY_CITY_LON = "city_lon";

    private String previousLabel;
    private long previousLat;
    private long previousLon;
    private boolean hadSavedCity;

    private Context context() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    /** Step 86: compute expected connectivity state from Android's ConnectivityManager. */
    private boolean systemIsOnline() {
        ConnectivityManager cm = (ConnectivityManager) context()
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null) return true;

        Network network = cm.getActiveNetwork();
        if (network == null) return false;

        NetworkCapabilities caps = cm.getNetworkCapabilities(network);
        return caps != null
                && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    /** Step 86: save a test city so launch stays deterministic without location dialogs. */
    @Before
    public void saveTestCityAndBackupPrefs() {
        SharedPreferences prefs = context().getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        hadSavedCity = prefs.contains(KEY_CITY_LABEL)
                && prefs.contains(KEY_CITY_LAT)
                && prefs.contains(KEY_CITY_LON);

        if (hadSavedCity) {
            previousLabel = prefs.getString(KEY_CITY_LABEL, null);
            previousLat = prefs.getLong(KEY_CITY_LAT, 0L);
            previousLon = prefs.getLong(KEY_CITY_LON, 0L);
        }

        prefs.edit()
                .putString(KEY_CITY_LABEL, "Faisalabad, Pakistan")
                .putLong(KEY_CITY_LAT, Double.doubleToRawLongBits(31.418))
                .putLong(KEY_CITY_LON, Double.doubleToRawLongBits(73.079))
                .commit();
    }

    /** Step 86: restore original saved city after each test. */
    @After
    public void restorePrefs() {
        SharedPreferences.Editor editor = context()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit();

        if (hadSavedCity) {
            editor.putString(KEY_CITY_LABEL, previousLabel)
                    .putLong(KEY_CITY_LAT, previousLat)
                    .putLong(KEY_CITY_LON, previousLon);
        } else {
            editor.remove(KEY_CITY_LABEL)
                    .remove(KEY_CITY_LAT)
                    .remove(KEY_CITY_LON);
        }
        editor.commit();
    }

    /** Step 86: ACCESS_NETWORK_STATE must be granted and isOnline() must match the system. */
    @Test
    public void connectivityCheckMatchesSystemState() {

        int state = ContextCompat.checkSelfPermission(
                context(), Manifest.permission.ACCESS_NETWORK_STATE);
        assertEquals("ACCESS_NETWORK_STATE permission must be granted",
                PackageManager.PERMISSION_GRANTED, state);

        boolean expected = systemIsOnline();

        try (ActivityScenario<MainActivity> scenario =
                     ActivityScenario.launch(MainActivity.class)) {

            scenario.onActivity(activity ->
                    assertEquals("isOnline() must match ConnectivityManager",
                            expected, activity.isOnline()));
        }
    }

    /** Step 86: launching offline shows the banner, keeps saved city, and does not spin forever. */
    @Test
    public void offlineLaunchShowsErrorBannerAndStopsSpinner() {

        Assume.assumeFalse("device is online — disable Wi-Fi and mobile data to run offline UI test",
                systemIsOnline());

        try (ActivityScenario<MainActivity> ignored =
                     ActivityScenario.launch(MainActivity.class)) {

            onView(withId(R.id.tvLocation)).check(matches(withText("Faisalabad, Pakistan")));
            onView(withId(R.id.llError)).check(matches(isDisplayed()));
            onView(withId(R.id.tvError)).check(matches(withText(
                    "You're offline. Showing the last loaded weather. Tap Retry when you're back online.")));
            onView(withId(R.id.progressBar)).check(matches(not(isDisplayed())));
            onView(withId(R.id.btnToggleSearch)).check(matches(isEnabled()));
        }
    }

    /** Step 86: searching offline shows the search error, unlocks buttons, and retry behaves cleanly. */
    @Test
    public void offlineSearchAndRetryLeaveControlsUsable() {

        Assume.assumeFalse("device is online — disable Wi-Fi and mobile data to run offline UI test",
                systemIsOnline());

        try (ActivityScenario<MainActivity> ignored =
                     ActivityScenario.launch(MainActivity.class)) {

            onView(withId(R.id.btnToggleSearch)).perform(click());
            onView(withId(R.id.etCitySearch))
                    .perform(replaceText("London"), closeSoftKeyboard());
            onView(withId(R.id.btnSearch)).perform(click());

            onView(withId(R.id.llError)).check(matches(isDisplayed()));
            onView(withId(R.id.tvError)).check(matches(withText(
                    "You're offline — city search needs an internet connection.")));
            onView(withId(R.id.progressBar)).check(matches(not(isDisplayed())));
            onView(withId(R.id.btnSearch)).check(matches(isEnabled()));
            onView(withId(R.id.btnToggleSearch)).check(matches(isEnabled()));

            onView(withId(R.id.btnRetry)).perform(click());

            onView(withId(R.id.tvError)).check(matches(withText(
                    "You're offline — city search needs an internet connection.")));
            onView(withId(R.id.progressBar)).check(matches(not(isDisplayed())));
            onView(withId(R.id.btnSearch)).check(matches(isEnabled()));
        }
    }
}
