# Release Notes

<!-- Step 100: release history -->

## v1.0 — Initial Release

### What's new

- **Live weather** — temperature, feels-like, humidity, wind speed, and precipitation via Open-Meteo API (no key required)
- **Hourly forecast** — 24-hour scrolling strip with weather icons
- **7-day forecast** — daily high/low with WMO-mapped icons and dividers
- **GPS location detection** — FusedLocationProviderClient with graceful permission handling
- **IP-based fallback** — automatic city detection via `ip-api.com` when GPS is denied
- **City search** — real-time Nominatim geocoding with result list; tap to load
- **Saved city** — last city persisted in SharedPreferences and restored on relaunch
- **Day/night theme** — colour palette switches automatically at sunrise/sunset
- **Pull-to-refresh** — drag down from the top of the scroll view to reload
- **Search lens toggle** — magnifying-glass button reveals/hides the search bar; no permanent bar on screen
- **Offline handling** — specific error messages for no connection, timeouts, and server errors
- **Accessibility** — content descriptions on all icons; scalable text
- **21 unit tests** — covering API responses, geocoding, offline mode, and location permission scenarios

### Known limitations

- APK build (release-signed) is deferred (Step 99); run via Android Studio or `gradlew assembleDebug`
- No widget or notification support in this version

### Dependencies

| Library | Version |
|---------|---------|
| Retrofit | 2.11.0 |
| Gson converter | 2.11.0 |
| Google Play Services Location | 21.3.0 |
| AndroidX RecyclerView | 1.4.0 |

### API endpoints used

| Service | URL |
|---------|-----|
| Open-Meteo weather | `https://api.open-meteo.com/v1/forecast` |
| Nominatim geocoding | `https://nominatim.openstreetmap.org/search` |
| ip-api location fallback | `http://ip-api.com/json` |
