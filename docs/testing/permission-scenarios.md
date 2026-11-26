# Step 85 — Location permission test matrix

Automated coverage: `app/src/androidTest/java/com/example/weatherapp/LocationPermissionTest.java`
Everything below must be checked by hand — Android's permission dialog cannot be driven reliably from a test.

Run the automated part on a connected device/emulator:

```
gradlew connectedDebugAndroidTest
```

---

## Reset state before a run

```powershell
$adb = "C:\Users\Ahmad Hassan\AppData\Local\Android\Sdk\platform-tools\adb.exe"

& $adb uninstall com.example.weatherapp          # wipes "deny forever" too
# or, without uninstalling:
& $adb shell pm revoke com.example.weatherapp android.permission.ACCESS_FINE_LOCATION
& $adb shell pm revoke com.example.weatherapp android.permission.ACCESS_COARSE_LOCATION
```

---

## Matrix

| # | Setup | Action | Expected screen | Expected Logcat |
|---|---|---|---|---|
| 1 | Fresh install | Launch | System dialog appears | `No permission — asking the user` |
| 2 | #1 | Tap **Allow** | Weather for GPS location; header shows real city | `Permission result: {...=true}` → `Permission granted — GPS path` → `GPS: lat, lon` |
| 3 | Fresh install | Launch → **Don't allow** | Weather for IP/default city | `Denied — will ask again next launch` → `Using default city — permission denied` |
| 4 | #3 | Launch again → **Don't allow** | Same fallback, no crash | `Denied — will ask again next launch` |
| 5 | #4 | Launch a 3rd time | **No dialog** | `Denied permanently — enable in Settings` |
| 6 | #5 | Settings → Apps → WeatherApp → Permissions → Location → **Allow** → return to app | Weather switches to GPS city **without restart** | `Permission granted while paused — reloading` |
| 7 | Permission granted | Settings → Permissions → **Don't allow** → return | Existing weather stays on screen | `Permission revoked while paused` |
| 8 | Permission denied | Tap **Refresh** | Fallback weather reloads, no dialog spam | `Refresh pressed` → `No permission — asking the user` |
| 9 | Permission denied | Search `Tokyo` → tap result | Tokyo weather loads (search needs no location permission) | `Loading weather for Tokyo...` |
| 10 | Permission denied + GPS-less | Launch | IP fallback used, header shows IP city | `GPS unavailable — ...` → `IP fallback started` → `IP location: ...` |

---

## What the automated tests cover

| Test | Asserts |
|---|---|
| `helperAgreesWithSystemPermissionState` | `hasPermission()` equals `(fine OR coarse)` from `ContextCompat` — catches a check that only looks at `ACCESS_FINE_LOCATION` |
| `requestLocationFailsFastWithoutPermission` | With no grant, the callback receives `"no permission"` within 2 s and never calls `onLocation` |
| `resolveCityNameAlwaysCallsBack` | Reverse geocoding always invokes `onCity` (with a name or `null`), never hangs or throws |

## What cannot be automated (and why)

- **The dialog choice** — Android's permission dialog is a system UI surface; tapping it in an Espresso test is flaky across OS versions.
- **"Deny forever"** — needs two real denials; `pm revoke` does not reliably recreate that state.
- **Grant while paused** — requires leaving the app to Settings; `onResume` re-entry is best verified by hand.
