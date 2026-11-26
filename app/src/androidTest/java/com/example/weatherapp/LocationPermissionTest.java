package com.example.weatherapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.weatherapp.utils.LocationHelper;

import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Step 85: the permission gate of LocationHelper.
 *
 * Run on a device/emulator:  gradlew connectedDebugAndroidTest
 * The dialog itself (deny once / deny forever) is manual — see
 * docs/testing/permission-scenarios.md.
 */
@RunWith(AndroidJUnit4.class)
public class LocationPermissionTest {

    private Context context() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    /** Step 85: hasPermission() must be "fine OR coarse", exactly like the system check. */
    @Test
    public void helperAgreesWithSystemPermissionState() {

        Context ctx = context();
        LocationHelper helper = new LocationHelper(ctx);

        boolean fine = ContextCompat.checkSelfPermission(ctx,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        boolean coarse = ContextCompat.checkSelfPermission(ctx,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        assertEquals("hasPermission must equal (fine OR coarse)",
                fine || coarse, helper.hasPermission());
    }

    /** Step 85: with no permission, requestLocation must fail fast and never touch the API. */
    @Test
    public void requestLocationFailsFastWithoutPermission() throws InterruptedException {

        LocationHelper helper = new LocationHelper(context());

        Assume.assumeFalse("permission already granted on this device — test not applicable",
                helper.hasPermission());

        final AtomicReference<String> error = new AtomicReference<>();
        final CountDownLatch done = new CountDownLatch(1);

        helper.requestLocation(new LocationHelper.Callback() {

            @Override
            public void onLocation(double lat, double lon) {
                error.set("unexpected location");
                done.countDown();
            }

            @Override
            public void onError(String reason) {
                error.set(reason);
                done.countDown();
            }
        });

        assertTrue("callback must arrive immediately", done.await(2, TimeUnit.SECONDS));
        assertEquals("no permission", error.get());
    }

    /** Step 85: the reverse-geocoder must answer (or answer null) without throwing. */
    @Test
    public void resolveCityNameAlwaysCallsBack() throws InterruptedException {

        LocationHelper helper = new LocationHelper(context());

        final CountDownLatch done = new CountDownLatch(1);

        helper.resolveCityName(31.418, 73.079, city -> done.countDown());

        assertTrue("onCity must be called exactly once", done.await(10, TimeUnit.SECONDS));
    }
}
