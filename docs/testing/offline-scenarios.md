# Step 86 — Offline / no-internet test matrix

Automated coverage:
- JVM unit tests: `app/src/test/java/com/example/weatherapp/OfflineNetworkTest.java`
- Device UI tests: `app/src/androidTest/java/com/example/weatherapp/OfflineModeTest.java`

Run the JVM tests:

```
gradlew test
```

Run the device tests (disable Wi-Fi and mobile data first for the offline UI checks):

```
gradlew connectedDebugAndroidTest
```

---

## Toggle connectivity from ADB

```powershell
$adb = "C:\Users\Ahmad Hassan\AppData\Local\Android\Sdk\platform-tools\adb.exe"

# Go offline
& $adb shell svc wifi disable
& $adb shell svc data disable

# Come back online
& $adb shell svc wifi enable
& $adb shell svc data enable
```

---

## Matrix

| # | Setup | Action | Expected screen | Expected Logcat |
|---|---|---|---|---|
| 1 | Offline + saved city | Launch app | Saved city in header; offline banner shown; spinner hidden; buttons enabled | `Restored city: ...` → `Offline — skipping weather request` |
| 2 | Offline + no saved city + permission denied | Launch app | IP fallback skipped; default city header (`Unknown location` if geocoder offline); offline banner shown | `IP fallback` skipped → `Using default city — permission denied + offline` → `Offline — skipping weather request` |
| 3 | Offline | Tap **Refresh** or pull down | Offline weather banner stays visible; no crash; spinner stays hidden | `Refresh pressed` → `Offline — skipping weather request` |
| 4 | Offline | Type `London` → tap **Search** | Banner shows `You're offline — city search needs an internet connection.`; spinner hides; **Search** and **Refresh** stay enabled | `Searching: London` → `Offline — skipping city search` |
| 5 | After #4, still offline | Tap **Retry** | Search offline banner re-appears; spinner stays hidden | `Retry tapped, failedSearch=true` → `Offline — skipping city search` |
| 6 | After #4, turn Wi-Fi/data back ON | Tap **Retry** | Matching cities list loads for `London` | `Retry tapped, failedSearch=true` → `Searching: London` → `Matches: ...` |
| 7 | After #1, turn Wi-Fi/data back ON | Tap **Retry** | Live weather loads and error banner hides | `Retry tapped, failedSearch=false` → `Request URL: ...` → `----- CURRENT WEATHER -----` |
| 8 | Online when request starts, drop Wi-Fi mid-flight | Tap **Refresh** then turn off Wi-Fi immediately | Banner shows DNS/connection error (`No internet connection...` or `Connection problem...`); spinner hides | `No internet or DNS failure` or `I/O problem during request` |

---

## What the automated tests cover

| Test | Suite | Asserts |
|---|---|---|
| `weatherRequestFailsWithIoExceptionWhenUnreachable` | `test` | `WeatherApi.getWeather` throws `IOException` instead of hanging or crashing when unreachable |
| `geocodingRequestFailsWithIoExceptionWhenUnreachable` | `test` | `GeocodingApi.searchCities` throws `IOException` when unreachable |
| `ipLocationRequestFailsWithIoExceptionWhenUnreachable` | `test` | `IpLocationApi.getLocation` throws `IOException` when unreachable |
| `apiErrorEnvelopeParsesReason` | `test` | `ApiError` parses Open-Meteo error JSON cleanly |
| `connectivityCheckMatchesSystemState` | `androidTest` | `ACCESS_NETWORK_STATE` is granted and `MainActivity.isOnline()` equals `ConnectivityManager` state |
| `offlineLaunchShowsErrorBannerAndStopsSpinner` | `androidTest` | When offline, launch shows the offline weather banner, keeps saved city, hides spinner, and leaves buttons enabled |
| `offlineSearchAndRetryLeaveControlsUsable` | `androidTest` | When offline, searching and tapping Retry show the offline search message and re-enable buttons (verifies Step 86 `endRequest()` fix) |

## What cannot be automated (and why)

- **Mid-flight Wi-Fi drop (`UnknownHostException` / `SocketTimeoutException` on live device)** — toggling radio state in the middle of a Retrofit call is timing-dependent on physical hardware.
- **Reconnecting and tapping Retry to load real weather** — needs live network toggle between assertions, which is unreliable across emulators/devices without test-harness permissions.
