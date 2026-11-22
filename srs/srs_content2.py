# -*- coding: utf-8 -*-
"""Sections 4-12 and the appendices of SRS.docx.

Cross-references use {t:key} for tables and {f:key} for figures; the builder
resolves them to the final numbers in document order.
"""

# --------------------------------------------------------------------------
# 4. EXTERNAL INTERFACE REQUIREMENTS
# --------------------------------------------------------------------------
EXTERNAL = [
    ("h1", "4  External interface requirements"),
    ("p", "This section defines the boundary between the application and everything it talks to: the person "
          "using it, the hardware it runs on, the libraries it links against and the services it calls."),

    ("h2", "4.1  User interfaces"),
    ("p", "Release 1.0 has one screen. It is a vertically scrolling page with four regions: a location header, "
          "a current-conditions card carrying the dominant temperature and condition, two metric tiles for "
          "humidity and wind, and two forecast regions (hourly strip and seven-day list). The layout was "
          "designed before the data layer and is already implemented in activity_main.xml, so the interface "
          "requirements below describe an artefact that exists rather than an aspiration."),
    ("fig", "fig_wire.png", "Home screen layout with its five functional regions; element identifiers refer to activity_main.xml", 6.3, "wire"),
    ("table", "User interface elements",
     ["Element ID", "Type", "Content and behaviour", "Data source"],
     [["tvLocation", "Text", "Active city or locality, bold, 21 sp; falls back to \"Your location\"",
       "Reverse geocoding or selected city"],
      ["ivCurrentWeather", "Image", "Condition illustration for the current reading",
       "Weather code and day flag"],
      ["tvTemperature", "Text", "Current temperature, 68 sp, rounded to a whole degree", "current.temperature_2m"],
      ["tvCondition", "Text", "Short condition phrase, for example \"Mostly Sunny\"", "current.weather_code"],
      ["tvFeelsLike", "Text", "\"Feels like N°\" from apparent temperature", "current.apparent_temperature"],
      ["tvHighLow", "Text", "\"H: n°   L: n°\" when the daily block is present", "daily.temperature_2m_max/min"],
      ["tvHumidity", "Text", "Relative humidity, whole per cent", "current.relative_humidity_2m"],
      ["tvWindSpeed", "Text", "Wind speed in the user's unit", "current.wind_speed_10m"],
      ["hourly strip", "Scroll container", "24 chips, each with time, icon and temperature", "hourly block"],
      ["dailyForecastContainer", "Container", "Seven rows, each with day, icon and max/min", "daily block"],
      ["status region", "Text / banner", "Load, stale and error messaging, including a retry action",
       "Application state"]],
     [1.3, 0.95, 2.85, 1.4]),

    ("h3", "4.1.1  Screen states"),
    ("p", "The screen has five states that must be distinguishable at a glance, because the same values can be "
          "fresh, stale or absent. Table {t:ui_states} defines them; the acceptance tests in Section 10 check "
          "each one."),
    ("table", "Screen states",
     ["State", "Trigger", "Presentation"],
     [["Loading", "First request in flight", "Skeleton or progress indicator in the card area; header still visible"],
      ["Fresh", "Successful response received less than 15 minutes ago", "Values and icons as returned; observation time shown"],
      ["Stale", "Cached data older than 15 minutes", "Values shown with an \"Updated N minutes ago\" label"],
      ["Empty", "No location and no cached result", "Prompt to grant location or search for a city"],
      ["Error", "Request failed and no usable cache", "Message naming the failure class plus a retry action"]],
     [1.1, 2.4, 3.0], "ui_states"),
    ("h3", "4.1.2  Interaction rules"),
    ("bullets", [
        "The screen scrolls vertically only; the hourly strip scrolls horizontally inside its region.",
        "Pull-to-refresh is available from the top of the page and does not scroll past the header.",
        "Every tappable target is at least 48 dp by 48 dp.",
        "Rotating the device preserves the displayed data and the hourly scroll position.",
        "System font scaling up to 200 per cent must not clip the temperature or the metric values.",
    ]),

    ("h2", "4.2  Hardware interfaces"),
    ("table", "Hardware interfaces",
     ["Interface", "Use", "Notes"],
     [["GNSS and network positioning", "Coordinate fix for the forecast", "Accessed only through the fused "
       "location provider; no direct GPS API use"],
      ["Network radio (Wi-Fi / cellular)", "HTTPS to the weather and geocoding endpoints", "Requests fail fast "
       "when no network is available"],
      ["Display", "Portrait screen, 4.7–6.9 in", "Layout uses dp units and scales with density"],
      ["Non-volatile storage", "Preferences and the last cached response", "Small footprint, tens of kilobytes"],
      ["Touch input", "Scrolling, pull-to-refresh, search", "No gestures beyond scrolling and tapping in 1.0"]],
     [1.55, 2.35, 2.6], "ext_hw"),

    ("h2", "4.3  Software interfaces"),
    ("p", "The application links against the Android framework, Google Play Services and four third-party "
          "libraries. Versions are pinned in the module build file so that a rebuild resolves the same "
          "artefacts."),
    ("table", "Software components",
     ["Component", "Version", "Role", "Interface"],
     [["Android SDK", "compileSdk 37, minSdk 24", "Platform APIs, resources, activity lifecycle", "Framework classes"],
      ["Java", "11", "Language and runtime level", "Source and target compatibility"],
      ["Retrofit", "2.11.0", "Type-safe HTTP client", "WeatherApi interface with @GET and @Query"],
      ["converter-gson", "2.11.0", "JSON to Java mapping", "Annotated model classes"],
      ["OkHttp", "4.x (transitive)", "HTTP transport, connection pooling, timeouts", "Configured through Retrofit"],
      ["Play Services Location", "21.3.0", "Fused location provider", "FusedLocationProvider client"],
      ["RecyclerView", "1.4.0", "Efficient list rendering for the daily rows", "Adapter and ViewHolder"],
      ["Material Components", "via version catalog", "Material 3 theming and components", "XML themes and widgets"],
      ["AndroidX AppCompat / Activity", "via version catalog", "Backward compatibility and edge-to-edge support", "AppCompatActivity"],
      ["JUnit 4 / Espresso", "via version catalog", "Unit and instrumented tests", "Test runners"]],
     [1.7, 1.25, 2.25, 1.8], "ext_sw"),

    ("h2", "4.4  Communication interfaces"),
    ("p", "All communication is client-initiated HTTPS over port 443 with JSON payloads encoded as UTF-8. There "
          "is no authentication: the service is public and key-less, so no credential is stored in the APK. "
          "Table {t:net} fixes the transport parameters; Section 6 defines the payload."),
    ("table", "Network interface parameters",
     ["Parameter", "Value"],
     [["Scheme and port", "HTTPS, 443, TLS 1.2 or later"],
      ["Hosts", "api.open-meteo.com (forecast); geocoding-api.open-meteo.com (city search)"],
      ["Methods", "GET only; no request body"],
      ["Media type", "application/json; charset=utf-8"],
      ["Connect timeout", "10 seconds"],
      ["Read timeout", "15 seconds"],
      ["Retry policy", "One retry for connection failures only; no retry for 4xx responses"],
      ["Concurrency", "At most one forecast request in flight; a newer request supersedes an older one"],
      ["Caching", "Application-level cache keyed by rounded coordinates; HTTP cache headers are not relied upon"],
      ["Attribution", "Data credited to Open-Meteo in the interface, as the CC BY 4.0 licence requires"]],
     [1.6, 4.9], "net"),
]

# --------------------------------------------------------------------------
# 5. NON-FUNCTIONAL REQUIREMENTS
# --------------------------------------------------------------------------
NFR = [
    ("h1", "5  Non-functional requirements"),
    ("p", "Quality attributes decide whether the product is pleasant to use, not merely functional. Each "
          "requirement below names a measurable target and the way it will be verified, because an "
          "unmeasurable quality requirement cannot be accepted or rejected."),
    ("p", "The targets assume a mid-range Android device released in the last four years, a 4G connection and "
          "a warm application process unless stated otherwise. Verification methods are abbreviated as T "
          "(test), I (inspection) and D (demonstration)."),

    ("h2", "5.1  Performance and responsiveness"),
    ("p", "Weather is glanceable information; anything slower than a couple of seconds feels broken. The "
          "budget is split between start-up, the network round trip and rendering, and no work that can block "
          "a frame is allowed on the main thread."),

    ("h2", "5.2  Reliability and availability"),
    ("p", "The product's availability is bounded by the external service, so the client's job is to degrade "
          "predictably: keep the last good result, label it, and never crash on an unexpected payload."),

    ("h2", "5.3  Security and privacy"),
    ("p", "The application collects no personal data, has no accounts and stores nothing about the user except "
          "the last selected city and display preferences. Location is used to build a request and is not "
          "persisted beyond the current session; coordinates in the log must be handled carefully because they "
          "are personal data under most privacy regimes."),

    ("h2", "5.4  Usability and accessibility"),
    ("p", "A weather screen is read in a few seconds, often one-handed and sometimes outdoors. Legibility, "
          "contrast and touch size therefore matter more than density of information."),

    ("h2", "5.5  Compatibility, maintainability and compliance"),
    ("p", "The application must run on a wide range of Android versions, remain cheap to change, and respect "
          "the terms under which its data is published."),

    ("table", "Non-functional requirements",
     ["ID", "Requirement", "Measure and verification", "MoSCoW"],
     [["Performance", "", "", ""],
      ["NFR-01", "Cold start to interactive screen shall not exceed 2.5 s on the reference device",
       "Measure with Android Studio profiler; T", "Must"],
      ["NFR-02", "First weather content shall be on screen within 3 s on 4G with a warm cache",
       "Timed instrumented run; T", "Should"],
      ["NFR-03", "No network, disk or JSON parsing shall run on the main thread",
       "StrictMode inspection and code review; I", "Must"],
      ["NFR-04", "Scrolling the page shall sustain 60 fps without dropped frames above 1 per cent",
       "Frame metrics from a GPU profile; T", "Should"],
      ["NFR-05", "The release APK shall not exceed 20 MB", "APK analyser; I", "Should"],
      ["NFR-06", "A refresh shall complete within 2 s when the response is served from cache",
       "Instrumented timing; T", "Could"],
      ["Reliability", "", "", ""],
      ["NFR-07", "The application shall not crash on malformed, partial or empty JSON",
       "Fault-injection tests with altered fixtures; T", "Must"],
      ["NFR-08", "A failed refresh shall leave the previous data visible and marked stale",
       "Manual and automated test; T", "Must"],
      ["NFR-09", "Crash-free session rate shall reach 99.5 per cent across the acceptance run",
       "Crash log review; I", "Should"],
      ["NFR-10", "Displayed data shall never be older than 15 minutes without an explicit staleness label",
       "Timing test with a clock offset; T", "Must"],
      ["Security and privacy", "", "", ""],
      ["NFR-11", "All traffic shall use HTTPS with TLS 1.2 or later; cleartext shall be disabled",
       "Network security configuration inspection; I", "Must"],
      ["NFR-12", "No API key, token or secret shall be stored in the repository or the APK",
       "Secret scan of the repository; I", "Must"],
      ["NFR-13", "Coordinates shall not be written to persistent logs in release builds",
       "Log inspection on a release build; T", "Must"],
      ["NFR-14", "Location permission shall be requested in context with a rationale, and only when needed",
       "Manual walkthrough on a clean install; D", "Must"],
      ["NFR-15", "Release builds shall be minified and resource-shrunk",
       "Build configuration inspection; I", "Should"],
      ["Usability and accessibility", "", "", ""],
      ["NFR-16", "The current temperature and condition shall be visible without scrolling on a 5.5-inch screen",
       "Screenshot review on the reference device; D", "Must"],
      ["NFR-17", "Reaching current conditions from a cold start, with permission granted, shall need no more than two taps",
       "Task analysis; D", "Must"],
      ["NFR-18", "Body text shall meet a 4.5:1 contrast ratio and large text 3:1 in both themes",
       "Contrast calculation; I", "Must"],
      ["NFR-19", "Every interactive element shall be at least 48 dp by 48 dp",
       "Layout inspection; I", "Must"],
      ["NFR-20", "Font scaling to 200 per cent shall not clip temperature, humidity or wind values",
       "Manual test at maximum font size; T", "Must"],
      ["NFR-21", "Every weather illustration shall carry a content description naming the condition",
       "Accessibility scanner; T", "Must"],
      ["Compatibility", "", "", ""],
      ["NFR-22", "The application shall install and run on API 24 through API 37",
       "Emulator matrix across API 24, 28, 33, 36; T", "Must"],
      ["NFR-23", "The layout shall render without overlap on screens from 4.7 to 6.9 inches",
       "Screenshot matrix; T", "Should"],
      ["NFR-24", "Light and dark appearances shall both be legible, following the system setting by default",
       "Visual review in both themes; D", "Must"],
      ["NFR-25", "The application shall remain usable with only an approximate (coarse) location",
       "Test with location accuracy reduced; T", "Must"],
      ["Maintainability", "", "", ""],
      ["NFR-26", "Network, model and presentation concerns shall remain in separate packages",
       "Package review; I", "Must"],
      ["NFR-27", "Model parsing shall be covered by unit tests, including unknown and missing fields",
       "Test report; T", "Should"],
      ["NFR-28", "The build shall succeed from a clean clone with the Gradle wrapper and no manual steps",
       "Clean build on a second machine; T", "Must"],
      ["NFR-29", "Android Lint shall report no errors and no more than five warnings of low severity",
       "Lint report; I", "Should"],
      ["NFR-30", "Every public class shall carry a short Javadoc comment describing its responsibility",
       "Code review; I", "Could"],
      ["Compliance", "", "", ""],
      ["NFR-31", "Weather data shall be visibly attributed to Open-Meteo in the interface",
       "Screenshot review; I", "Must"],
      ["NFR-32", "The application shall not present data as its own or sublicense it",
       "Review of the attribution text; I", "Must"],
      ["NFR-33", "The application shall collect no personal data beyond an optional location fix",
       "Manifest and code review; I", "Must"]],
     [0.62, 3.2, 1.88, 0.8], "nfr"),
]

# --------------------------------------------------------------------------
# 6. DATA AND API DESIGN
# --------------------------------------------------------------------------
DATA = [
    ("h1", "6  Data requirements and API contract"),
    ("p", "The application owns very little data: the JSON document that arrives from the service, the typed "
          "model it is parsed into, the small cache derived from it and a handful of preferences. This section "
          "defines that data precisely, because the boundary between JSON keys and Java fields is where most "
          "weather clients break."),

    ("h2", "6.1  Endpoints"),
    ("table", "External endpoints",
     ["Purpose", "Endpoint", "Method", "Auth"],
     [["Forecast for a coordinate pair", "https://api.open-meteo.com/v1/forecast", "GET", "None"],
      ["City search (planned)", "https://geocoding-api.open-meteo.com/v1/search", "GET", "None"],
      ["Reverse geocoding (planned)", "Device Geocoder, or a geocoding lookup by coordinate", "Local", "None"]],
     [1.75, 3.15, 0.6, 0.9], "api_endpoints"),

    ("h2", "6.2  Forecast request parameters"),
    ("table", "Forecast request parameters",
     ["Parameter", "Type", "Required", "Value in release 1.0"],
     [["latitude", "decimal", "Yes", "Active location latitude, in decimal degrees"],
      ["longitude", "decimal", "Yes", "Active location longitude, in decimal degrees"],
      ["current", "comma-separated list", "Yes",
       "temperature_2m, relative_humidity_2m, apparent_temperature, is_day, precipitation, weather_code, wind_speed_10m"],
      ["hourly", "comma-separated list", "No",
       "temperature_2m, precipitation_probability, weather_code (24 hours)"],
      ["daily", "comma-separated list", "No",
       "weather_code, temperature_2m_max, temperature_2m_min, precipitation_sum (7 days)"],
      ["timezone", "string", "No", "auto — the service returns local times for the coordinate"],
      ["forecast_days", "integer", "No", "1 for the current view; 7 when the daily block is requested"],
      ["wind_speed_unit", "string", "No", "kmh, so no client-side conversion is needed for wind"],
      ["temperature_unit", "string", "No", "celsius; the imperial preference is applied at render time"]],
     [1.25, 1.35, 0.85, 3.05], "api_params"),
    ("p", "A concrete request for the default location used during development, Faisalabad (31.418 N, "
          "73.079 E), looks like this:"),
    ("code", "GET https://api.open-meteo.com/v1/forecast\n"
             "      ?latitude=31.418&longitude=73.079\n"
             "      &current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,\n"
             "               precipitation,weather_code,wind_speed_10m\n"
             "      &hourly=temperature_2m,precipitation_probability,weather_code\n"
             "      &daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_sum\n"
             "      &timezone=auto&forecast_days=7&wind_speed_unit=kmh\n"
             "Accept: application/json"),

    ("h2", "6.3  Weather codes"),
    ("p", "The service reports conditions as WMO codes. Table {t:wmo} is the complete mapping used by the "
          "client, including the illustration family selected for each code. Unlisted codes fall back to the "
          "neutral branch required by FR-35."),
    ("table", "WMO weather-code mapping",
     ["Code(s)", "Condition", "Phrase shown", "Illustration family"],
     [["0", "Clear sky", "Clear", "clear (sun or moon)"],
      ["1, 2", "Mainly clear, partly cloudy", "Mostly Sunny / Partly Cloudy", "partly cloudy"],
      ["3", "Overcast", "Cloudy", "cloudy"],
      ["45, 48", "Fog and depositing rime fog", "Fog", "cloudy (dimmed)"],
      ["51, 53, 55", "Drizzle: light, moderate, dense", "Drizzle", "rain"],
      ["56, 57", "Freezing drizzle", "Freezing Drizzle", "rain (cold tint)"],
      ["61, 63, 65", "Rain: slight, moderate, heavy", "Rain", "rain"],
      ["66, 67", "Freezing rain", "Freezing Rain", "rain (cold tint)"],
      ["71, 73, 75", "Snow fall: slight, moderate, heavy", "Snow", "snow"],
      ["77", "Snow grains", "Snow Grains", "snow"],
      ["80, 81, 82", "Rain showers: slight, moderate, violent", "Showers", "rain"],
      ["85, 86", "Snow showers", "Snow Showers", "snow"],
      ["95", "Thunderstorm", "Thunderstorm", "thunder"],
      ["96, 99", "Thunderstorm with hail", "Thunderstorm, Hail", "thunder"],
      ["other", "Unrecognised", "Unknown", "neutral"]],
     [0.95, 2.15, 1.95, 1.45], "wmo"),

    ("h2", "6.4  Response model and data dictionary"),
    ("p", "The response is parsed by Gson into WeatherResponse and its nested CurrentWeather class. Each JSON "
          "key is pinned with @SerializedName, so a rename in the service surfaces as a null field rather than "
          "a silently wrong value. Table {t:data} is the data dictionary for the fields the client consumes."),
    ("table", "Data dictionary for the consumed response fields",
     ["JSON key", "Java field", "Type", "Unit", "Used by"],
     [["latitude", "latitude", "double", "degrees", "Internal consistency check"],
      ["longitude", "longitude", "double", "degrees", "Internal consistency check"],
      ["timezone", "timezone", "String", "IANA name", "Hour formatting"],
      ["current.time", "current.time", "String", "ISO 8601 local", "Observation timestamp, FR-12"],
      ["current.temperature_2m", "current.temperature", "double", "°C", "tvTemperature, FR-08"],
      ["current.relative_humidity_2m", "current.humidity", "int", "%", "tvHumidity, FR-13"],
      ["current.apparent_temperature", "current.feelsLike", "double", "°C", "tvFeelsLike, FR-10"],
      ["current.is_day", "current.isDay", "int (0/1)", "flag", "Icon selection, FR-34"],
      ["current.precipitation", "current.precipitation", "double", "mm", "Precipitation label, FR-15"],
      ["current.weather_code", "current.weatherCode", "int", "WMO code", "Condition phrase and icon, FR-09"],
      ["current.wind_speed_10m", "current.windSpeed", "double", "km/h", "tvWindSpeed, FR-14"],
      ["hourly.time[]", "—", "String[]", "ISO 8601", "Hour labels, FR-17"],
      ["hourly.temperature_2m[]", "—", "double[]", "°C", "Hourly chips, FR-17"],
      ["daily.time[]", "—", "String[]", "ISO 8601 date", "Day names, FR-21"],
      ["daily.temperature_2m_max[] / _min[]", "—", "double[]", "°C", "Daily range, FR-21"],
      ["daily.weather_code[]", "—", "int[]", "WMO code", "Daily icon, FR-21"]],
     [2.05, 1.55, 0.95, 0.9, 1.35], "data"),

    ("h2", "6.5  Cache and preference data"),
    ("p", "Caching is a single-entry store per location in SharedPreferences: the raw JSON of the last "
          "successful response, the timestamp of that response, the rounded coordinate pair and the locality "
          "label. Rounding to two decimal places (about 1.1 km at the equator) makes the key stable against "
          "small location jitter while remaining finer than the forecast grid, so a cache hit is almost always "
          "semantically valid."),
    ("table", "Persisted data",
     ["Key", "Content", "Lifetime", "Privacy note"],
     [["last_response", "Raw JSON of the last successful forecast", "Until superseded or 30 days",
       "Contains coordinates; excluded from backup rules"],
      ["last_response_at", "Epoch milliseconds of the response", "As above", "Not personal by itself"],
      ["active_location", "Latitude, longitude and locality label", "Until the user changes city",
       "Location data; the only personal datum retained"],
      ["unit_system", "metric or imperial", "Permanent", "Not personal"],
      ["theme_mode", "system, light or dark", "Permanent", "Not personal"],
      ["last_city", "Selected city for search mode", "Permanent", "User-chosen, not sensed"]],
     [1.2, 2.4, 1.7, 1.5], "persist"),
]

# --------------------------------------------------------------------------
# 7. ARCHITECTURE AND DESIGN
# --------------------------------------------------------------------------
ARCH = [
    ("h1", "7  System architecture and design"),
    ("p", "The design follows the smallest structure that keeps the network, the data model and the screen "
          "apart. There is no dependency-injection framework, no view model persistence layer and no database: "
          "the product is small enough that these would add more surface than they remove."),

    ("h2", "7.1  Architectural overview"),
    ("p", "The application is organised in four layers. Presentation holds the activity and the XML resources. "
          "Networking holds the Retrofit singleton and the annotated endpoint interface. The model layer holds "
          "the parsed response types. The platform layer is the Android runtime, Play Services and the declared "
          "permissions. Dependencies point downward only: the activity knows about the API and the model, the "
          "model knows nothing about the network, and nothing knows about the activity."),
    ("fig", "fig_arch.png", "Layered architecture of the application and its single external dependency", 6.2, "arch"),
    ("table", "Components and responsibilities",
     ["Component", "Responsibility", "Depends on", "Requirements served"],
     [["MainActivity", "Owns the screen, triggers requests, binds results to views, hosts permission flow",
       "RetrofitClient, WeatherResponse", "FR-07, FR-28, FR-36"],
      ["activity_main.xml and resources", "Layout, strings, colours, themes, illustrations", "—",
       "FR-42, NFR-24"],
      ["RetrofitClient", "Builds and caches the Retrofit instance with base URL and Gson converter", "Retrofit",
       "NFR-26"],
      ["WeatherApi", "Declares the forecast endpoint and its query parameters", "Retrofit annotations",
       "FR-07, FR-16, FR-20"],
      ["WeatherResponse", "Top-level response: coordinates, timezone and the current block", "Gson",
       "FR-07"],
      ["CurrentWeather", "Current measurements bound to the main card", "Gson", "FR-08 – FR-14"],
      ["ConditionMapper (planned)", "Maps weather codes to phrases and illustration resources", "Resources",
       "FR-33, FR-34"],
      ["LocationClient (planned)", "Wraps the fused provider and permission state", "Play Services", "FR-01 – FR-06"],
      ["ForecastCache (planned)", "Stores and ages the last successful response", "SharedPreferences",
       "FR-30, FR-31"]],
     [1.5, 2.6, 1.35, 1.05], "layers"),

    ("h2", "7.2  Class design"),
    ("p", "Five types carry the design: two for transport, two for data and one for the screen. The diagram "
          "shows the intended encapsulation of release 1.0; the current implementation exposes public fields "
          "for brevity, which is acceptable while the model is small but should be tightened once binding is "
          "implemented."),
    ("fig", "fig_class.png", "Class diagram of the implemented types and their relationships", 6.2, "class"),
    ("table", "Class responsibilities",
     ["Class", "Kind", "Key members", "Notes"],
     [["RetrofitClient", "Final utility class", "BASE_URL, retrofit, getApi()",
       "Lazily creates one Retrofit instance; the singleton avoids rebuilding the OkHttp pool per call"],
      ["WeatherApi", "Interface", "getWeather(latitude, longitude, current, hourly, daily, timezone, forecastDays)",
       "One method today; additional endpoints (geocoding) will be added here"],
      ["WeatherResponse", "Model", "latitude, longitude, timezone, current", "Mirrors the top level of the JSON"],
      ["CurrentWeather", "Nested model", "time, temperature, humidity, feelsLike, isDay, precipitation, weatherCode, windSpeed",
       "Field names are pinned with @SerializedName"],
      ["MainActivity", "Activity", "onCreate, request, callback",
       "Will grow a binding method and a state renderer in the next iteration"]],
     [1.35, 1.1, 2.6, 1.45], "classes"),

    ("h2", "7.3  Dynamic behaviour"),
    ("p", "Figure {f:sequence} shows the collaboration for the main use case. The activity asks for a location, "
          "issues the request through the Retrofit proxy, receives a typed response and renders it. The diagram "
          "also records the current gap: the present build logs the outcome instead of binding it, which is the "
          "next implementation task."),
    ("fig", "fig_sequence.png", "Sequence of the main use case, from launch to rendered forecast", 6.3, "sequence"),
    ("fig", "fig_flow.png", "Activity flow for loading the forecast, including degraded paths", 5.0, "flow"),
    ("p", "Figure {f:flow} shows the same behaviour as a decision flow, including the paths that the "
          "requirements treat as first-class: permission denied, no coordinate fix, and a failing service. "
          "The flow is the basis for the test cases in Section 10, one case per decision outcome."),

    ("h2", "7.4  Design decisions and rationale"),
    ("table", "Design decisions",
     ["Decision", "Alternatives considered", "Rationale"],
     [["Retrofit with a declarative interface", "Raw HttpURLConnection, Volley, Ktor client",
       "Removes manual URL building and JSON plumbing; the interface documents the contract in code"],
      ["Gson via converter factory", "Moshi, manual JSONObject parsing",
       "Fewest moving parts for a small fixed schema; @SerializedName makes the mapping explicit"],
      ["Open-Meteo as the data source", "OpenWeatherMap, WeatherAPI, visual crossing",
       "No key, no quota negotiation, no billing account, CC BY 4.0 data with clear attribution"],
      ["Single activity with a scrolling page", "Multiple activities, fragments with bottom navigation",
       "One screen of content does not justify navigation structure; keeps state in one place"],
      ["Coordinates requested at API 1-day horizon", "Fetching seven days on first paint",
       "Smaller payload and faster first paint; the daily block is requested when it is displayed"],
      ["Client-side unit conversion", "Requesting the unit system from the service",
       "One cached response serves both display modes, so switching units costs no network call"],
      ["SharedPreferences cache", "Room database, DataStore, no cache",
       "Single-entry cache with two timestamps does not need a database; a schema migration would cost more "
       "than it returns"]],
     [1.75, 2.15, 2.6], "decisions"),
]

# --------------------------------------------------------------------------
# 8. USE CASES
# --------------------------------------------------------------------------
USECASES = [
    ("h1", "8  Use cases"),
    ("p", "The use case model describes the system from outside, in terms of what an actor can accomplish. "
          "The nine use cases below map onto the features of Section 3 and the functional requirements of "
          "Table {t:fr}."),
    ("fig", "fig_usecase.png", "Use case diagram; «include» relations show behaviour that is always performed, "
            "«extend» the optional failure path", 5.6, "usecase"),
    ("table", "Use case catalogue",
     ["ID", "Use case", "Actor", "Trigger", "Outcome", "Req."],
     [["UC-01", "View current weather", "User", "Launch or return to the app",
       "Current conditions for the active location are on screen", "FR-07 – FR-12"],
      ["UC-02", "View hourly forecast", "User", "Scroll to the hourly strip",
       "24 hourly entries are shown with the current hour highlighted", "FR-16 – FR-19"],
      ["UC-03", "View daily forecast", "User", "Scroll to the daily list",
       "Seven daily rows are shown with ranges and icons", "FR-20 – FR-22"],
      ["UC-04", "Refresh weather data", "User", "Pull down",
       "Data is re-fetched and the timestamp is updated", "FR-28 – FR-32"],
      ["UC-05", "Search city", "User", "Tap search and type a name",
       "Weather for the selected city replaces the current view", "FR-23 – FR-27"],
      ["UC-06", "Change units or theme", "User", "Open preferences",
       "Displayed values and appearance change without a restart", "FR-39, FR-40"],
      ["UC-07", "Detect location", "System / User", "First need for a location",
       "A coordinate pair and locality label become the active location", "FR-01 – FR-06"],
      ["UC-08", "Fetch forecast", "System", "Any need for weather data",
       "A valid response is parsed into the model", "FR-07, FR-16, FR-20"],
      ["UC-09", "Handle failure", "System", "Any request that cannot complete",
       "The user sees a specific message and a recovery action", "FR-36 – FR-38"]],
     [0.6, 1.3, 0.75, 1.3, 1.95, 0.65], "uc_catalogue"),

    ("h2", "8.1  UC-01 View current weather — expanded"),
    ("p", "The main use case is specified in full because it exercises every layer and every failure path. The "
          "other use cases follow the same template and are catalogued above."),
    ("table", "UC-01 specification",
     ["Field", "Description"],
     [["Identifier", "UC-01"],
      ["Name", "View current weather"],
      ["Primary actor", "User"],
      ["Supporting actors", "Location provider (Play Services), Open-Meteo forecast API"],
      ["Goal", "Know the current conditions at the active location within a few seconds of opening the app"],
      ["Preconditions", "Application installed; a network connection is available for fresh data"],
      ["Trigger", "The user launches the application or returns to it after the freshness window"],
      ["Main success scenario",
       "1. The application shows the screen with the last known location.\n"
       "2. It checks permission; when granted it obtains coordinates (UC-07).\n"
       "3. It builds the request and calls the forecast endpoint (UC-08).\n"
       "4. The service returns a JSON document with the current block.\n"
       "5. The client parses it into WeatherResponse.\n"
       "6. Temperature, condition, feels-like, humidity and wind are bound to their views.\n"
       "7. The illustration is selected from the weather code and the day flag.\n"
       "8. The observation time is displayed."],
      ["Extensions",
       "2a. Permission denied — the application offers city search and continues there.\n"
       "2b. No fix within five seconds — fall back to network location, then to the cached city.\n"
       "3a. No connectivity — show cached data with a stale label and a retry action.\n"
       "4a. HTTP error — surface the status class and offer retry.\n"
       "5a. Malformed payload — keep the previous data, log the failure, offer retry."],
      ["Postconditions",
       "Success: the screen shows the current reading for the active location, with its observation time.\n"
       "Failure: previously displayed data remains, marked as stale, and a retry path is available."],
      ["Non-functional notes", "NFR-01, NFR-02, NFR-07, NFR-10, NFR-16"],
      ["Requirements", "FR-07 – FR-12, FR-33 – FR-38"]],
     [1.35, 5.15], "uc01"),
]

# --------------------------------------------------------------------------
# 9. TRACEABILITY
# --------------------------------------------------------------------------
TRACE = [
    ("h1", "9  Traceability"),
    ("p", "A requirement is only useful if it can be followed from statement to implementation to test. Table "
          "{t:trace} records that chain for the release-blocking features; the same structure applies to the "
          "remainder of Table {t:fr}."),
    ("table", "Traceability from requirement to design and verification",
     ["Requirement group", "Use case", "Design element", "Test cases", "Section"],
     [["FR-01 – FR-06 location", "UC-07", "LocationClient, permission flow, MainActivity", "TC-03, TC-04, TC-05", "3.2"],
      ["FR-07 – FR-12 current conditions", "UC-01", "MainActivity, WeatherApi, CurrentWeather", "TC-01, TC-02, TC-06", "3.3"],
      ["FR-13 – FR-15 measurements", "UC-01", "Binding layer, unit converter", "TC-07, TC-08", "3.4"],
      ["FR-16 – FR-19 hourly", "UC-02", "Hourly adapter, layout strip", "TC-09, TC-10", "3.5"],
      ["FR-20 – FR-22 daily", "UC-03", "Daily adapter, RecyclerView", "TC-11, TC-12", "3.6"],
      ["FR-23 – FR-27 search", "UC-05", "Search UI, geocoding endpoint, persistence", "TC-13, TC-14, TC-15", "3.7"],
      ["FR-28 – FR-32 refresh and cache", "UC-04", "ForecastCache, refresh controller", "TC-16, TC-17", "3.8"],
      ["FR-33 – FR-35 condition mapping", "UC-01, UC-02, UC-03", "ConditionMapper, drawable set", "TC-18, TC-19", "3.9"],
      ["FR-36 – FR-38 error handling", "UC-09", "State renderer, retry action", "TC-20, TC-21, TC-22", "3.10"],
      ["FR-39 – FR-40 preferences", "UC-06", "Preference store, render-time conversion", "TC-23, TC-24", "3.11"],
      ["FR-41 – FR-43 accessibility and attribution", "All", "Content descriptions, string resources, footer", "TC-25, TC-26", "3.12"]],
     [1.85, 0.95, 2.2, 1.1, 0.55], "trace"),
]

# --------------------------------------------------------------------------
# 10. VERIFICATION
# --------------------------------------------------------------------------
TEST = [
    ("h1", "10  Verification and test plan"),
    ("p", "Verification has three layers. Unit tests cover the parsing and mapping logic, which is where "
          "regressions hide. Instrumented tests exercise the activity against a stubbed response. Manual "
          "walkthroughs cover everything that depends on a real device, a real permission dialog or a human "
          "reading the screen. The cases below are derived from the requirement identifiers, not from the "
          "implementation, so that a rewritten implementation is judged by the same criteria."),

    ("h2", "10.1  Test approach"),
    ("bullets", [
        "Unit: JUnit runs on the JVM against fixture JSON captured from the live service.",
        "Integration: Espresso drives the activity with a recording HTTP interceptor returning fixtures.",
        "Manual: permission dialogs, network toggling, font scaling, both themes, a physical device.",
        "Regression: every fixed defect adds a fixture or a case rather than a manual note.",
        "Exit criterion: all Must requirements covered by at least one passing case; no open severity-1 defects.",
    ]),

    ("h2", "10.2  Test cases"),
    ("table", "Test cases",
     ["ID", "Objective", "Precondition and steps", "Expected result", "Req."],
     [["TC-01", "Happy path, first launch",
       "Permission granted, network available; launch the app",
       "Current conditions appear within 3 s; no error state", "FR-07"],
      ["TC-02", "Temperature formatting",
       "Response with 28.4 °C; open the screen", "Displays 28°, not 28.4°", "FR-08"],
      ["TC-03", "Permission denial fallback",
       "Deny location; launch", "City search path offered; no crash; clear explanation", "FR-03"],
      ["TC-04", "Approximate location",
       "Grant coarse-only on API 33; launch", "Forecast is fetched for the approximate fix", "FR-06"],
      ["TC-05", "No fix within five seconds",
       "Fix suppressed; launch", "Fallback location used, or stale city shown with label", "FR-04"],
      ["TC-06", "Observation time shown",
       "Any successful response", "Time from current.time is visible and plausible", "FR-12"],
      ["TC-07", "Humidity rendering",
       "Fixture with 65 % humidity", "Tile shows 65 %", "FR-13"],
      ["TC-08", "Wind conversion",
       "Fixture with 3.3 m/s wind", "Tile shows about 12 km/h", "FR-14"],
      ["TC-09", "Hourly count and order",
       "Fixture with 24 hourly entries", "Exactly 24 chips, ascending time, current hour highlighted", "FR-16, FR-17"],
      ["TC-10", "Hourly scroll position",
       "Scroll the strip, rotate the device", "Scroll position is preserved", "FR-19"],
      ["TC-11", "Daily rows",
       "Fixture with 7 daily entries", "Seven rows, first labelled Today, max/min correct", "FR-20, FR-21"],
      ["TC-12", "Daily partial data",
       "Fixture with 3 days", "Three rows rendered; no empty rows or crash", "FR-21"],
      ["TC-13", "Search minimum length",
       "Type one character", "No request is issued", "FR-24"],
      ["TC-14", "Search disambiguation",
       "Search a duplicated city name", "Matches show region and country", "FR-25"],
      ["TC-15", "City persistence",
       "Select a city, kill and relaunch", "The selected city is restored", "FR-27"],
      ["TC-16", "Pull to refresh",
       "Pull down on a loaded screen", "Spinner shows, data refreshes, timestamp updates", "FR-28"],
      ["TC-17", "Stale label",
       "Serve a 20-minute-old cache with no network", "Values shown with an age label", "FR-31"],
      ["TC-18", "Unknown weather code",
       "Fixture with code 4", "Neutral icon and \"Unknown\" phrase; no crash", "FR-35"],
      ["TC-19", "Night icon",
       "Fixture with is_day = 0 and code 0", "Moon illustration, not sun", "FR-34"],
      ["TC-20", "Timeout handling",
       "Delay the response beyond 15 s", "Timeout message with retry; previous data retained", "FR-36"],
      ["TC-21", "HTTP 500 handling",
       "Serve a 500 response", "Error message naming the failure; retry works", "FR-36, FR-37"],
      ["TC-22", "Malformed JSON",
       "Serve truncated JSON", "No crash; error state; last good data retained", "NFR-07"],
      ["TC-23", "Unit switch",
       "Switch to imperial", "Values convert, network request unchanged", "FR-39"],
      ["TC-24", "Theme switch",
       "Force dark mode", "All text and cards remain legible; no hard-coded white", "FR-40"],
      ["TC-25", "Font scaling",
       "Set system font to 200 per cent", "No clipped temperature or metric values", "NFR-20"],
      ["TC-26", "Attribution present",
       "Open the screen", "Open-Meteo credit is visible", "FR-43"]],
     [0.55, 1.45, 1.95, 2.0, 0.55], "tests"),

    ("h2", "10.3  Acceptance criteria"),
    ("p", "Release 1.0 is accepted when every Must requirement has a passing case, when the application "
          "survives the three degraded scenarios (no permission, no network, failing service) without a crash, "
          "and when the screen remains legible in dark mode at maximum font size. Should requirements are "
          "accepted individually; a deferred Should requirement is recorded against Section 12 rather than "
          "silently dropped."),
]

# --------------------------------------------------------------------------
# 11. PLAN AND RISKS
# --------------------------------------------------------------------------
PLAN = [
    ("h1", "11  Project plan and risks"),
    ("p", "The work is organised in nine weeks with four milestone reviews. Requirements, layout and the "
          "network layer are already complete; location integration is in progress; the remaining features "
          "follow the order in Figure {f:gantt}, because each depends on the one before it."),
    ("fig", "fig_gantt.png", "Work breakdown and milestones for the nine-week schedule", 6.2, "gantt"),
    ("table", "Milestones",
     ["Milestone", "Week", "Exit criterion"],
     [["M1 — Requirements baseline", "W2", "This document reviewed; scope and priorities agreed"],
      ["M2 — Vertical slice", "W4", "Live data for a fixed coordinate rendered on the real layout"],
      ["M3 — Feature complete", "W7", "Location, search, hourly and daily views working end to end"],
      ["M4 — Release candidate", "W9", "Test plan executed; documentation complete; APK signed"]],
     [1.75, 0.6, 4.15], "milestones"),
    ("table", "Risk register",
     ["ID", "Risk", "Likelihood", "Impact", "Mitigation"],
     [["R-01", "Open-Meteo changes or withdraws the endpoint",
       "Low", "High", "Keep endpoint strings in one place; the model layer isolates the change; monitor release notes"],
      ["R-02", "Rate limiting during development", "Medium", "Medium",
       "Cache aggressively, throttle manual testing, use fixtures rather than live calls for repeat tests"],
      ["R-03", "Location permission denial is common with real users",
       "High", "Medium", "City search is a first-class path, not an afterthought; deny path is tested"],
      ["R-04", "Emulator provides no reliable GPS fix", "High", "Low",
       "Set a mock location in the emulator; expose a debug coordinate override"],
      ["R-05", "Android 12+ approximate-only location reduces forecast relevance",
       "Medium", "Low", "Accept the fix; the forecast grid is coarser than the error"],
      ["R-06", "Scope creep from extra features (radar, air quality)",
       "High", "Medium", "Section 12 records deferrals; the baseline is frozen at M1"],
      ["R-07", "Time pressure near submission", "Medium", "High",
       "Must requirements scheduled first; Should items are explicitly droppable"],
      ["R-08", "Third-party library upgrade breaks the build", "Low", "Medium",
       "Versions pinned in the version catalog; clean-build check at each milestone"]],
     [0.55, 2.15, 0.8, 0.7, 2.3], "risks"),
]

# --------------------------------------------------------------------------
# 12. FUTURE SCOPE + APPENDICES
# --------------------------------------------------------------------------
FUTURE = [
    ("h1", "12  Deferred scope and future releases"),
    ("p", "The following items were considered and deliberately excluded from release 1.0. They are recorded "
          "here so that a reviewer can see that the exclusions were decisions rather than oversights, and so "
          "that the architecture can leave room for them."),
    ("table", "Deferred features",
     ["Candidate", "Value", "Why deferred", "Target release"],
     [["Multiple saved cities with reordering", "Removes repeated searching for travellers",
       "Needs a schema and a management screen", "1.1"],
      ["Home-screen widget", "Weather without opening the app", "Separate lifecycle and update scheduling", "1.1"],
      ["Severe-weather notification", "Timely warning", "Requires background execution and a policy review", "1.2"],
      ["Air quality and UV index", "Health decisions", "Additional API blocks and a second layout region", "1.2"],
      ["Radar and precipitation map", "Nowcasting", "Mapping SDK, licensing, significantly larger APK", "2.0"],
      ["Urdu and other localisations", "Accessibility for local users", "Translation and layout testing effort", "1.1"],
      ["Tablet-optimised layout", "Better use of large screens", "Requires a second layout variant", "1.2"],
      ["Offline history and charts", "Trend awareness", "Needs a database and chart rendering", "2.0"]],
     [1.85, 1.75, 2.1, 0.8], "future"),
]

APPENDIX = [
    ("h1", "Appendix A  Sample response"),
    ("p", "The document below is an abridged response from the forecast endpoint for the development location, "
          "with the arrays shortened. It is the fixture used by the parsing tests."),
    ("code", "{\n"
             "  \"latitude\": 31.418,\n"
             "  \"longitude\": 73.079,\n"
             "  \"timezone\": \"Asia/Karachi\",\n"
             "  \"current\": {\n"
             "    \"time\": \"2026-11-22T15:00\",\n"
             "    \"temperature_2m\": 26.4,\n"
             "    \"relative_humidity_2m\": 41,\n"
             "    \"apparent_temperature\": 25.1,\n"
             "    \"is_day\": 1,\n"
             "    \"precipitation\": 0.0,\n"
             "    \"weather_code\": 1,\n"
             "    \"wind_speed_10m\": 9.7\n"
             "  },\n"
             "  \"hourly\": {\n"
             "    \"time\": [\"2026-11-22T15:00\", \"2026-11-22T16:00\", \"...\"],\n"
             "    \"temperature_2m\": [26.4, 25.8, \"...\"],\n"
             "    \"precipitation_probability\": [0, 0, \"...\"],\n"
             "    \"weather_code\": [1, 2, \"...\"]\n"
             "  },\n"
             "  \"daily\": {\n"
             "    \"time\": [\"2026-11-22\", \"2026-11-23\", \"...\"],\n"
             "    \"weather_code\": [1, 3, \"...\"],\n"
             "    \"temperature_2m_max\": [28.2, 27.5, \"...\"],\n"
             "    \"temperature_2m_min\": [16.1, 15.7, \"...\"],\n"
             "    \"precipitation_sum\": [0.0, 0.4, \"...\"]\n"
             "  }\n"
             "}"),

    ("h1", "Appendix B  Requirement summary"),
    ("p", "Table {t:prio} counts the requirements by priority. The totals are the basis for the release "
          "decision described in Section 10.3."),
    ("table", "Requirement counts by priority",
     ["Category", "Must", "Should", "Could", "Total"],
     [["Functional (FR)", "27", "14", "2", "43"],
      ["Non-functional (NFR)", "24", "8", "1", "33"],
      ["Total", "51", "22", "3", "76"]],
     [2.2, 1.0, 1.0, 1.0, 1.0], "prio"),
    ("p", "Functional requirements are grouped by feature in Table {t:fr}; non-functional requirements by "
          "category in Table {t:nfr}. Appendix C tracks progress against the same identifiers."),

    ("h1c", "Appendix C  Implementation checklist"),
    ("p", "The checklist below is the working progress view of this specification. It is updated as each "
          "requirement group is implemented and tested; the status column mirrors Table {t:status}."),
    ("table", "Implementation checklist",
     ["Area", "Required work", "Status"],
     [["Layout and resources", "Complete layout, themes, illustrations", "Done"],
      ["Network layer", "Retrofit client, endpoint interface, model", "Done"],
      ["Binding", "Replace preview values with live data; state renderer", "In progress"],
      ["Location", "Permission flow, fused provider, fallback to search", "In progress"],
      ["Hourly and daily adapters", "Populate strips from the response arrays", "Not started"],
      ["Search", "Geocoding, debounce, persistence", "Not started"],
      ["Cache and staleness", "Single-entry cache, age label", "Not started"],
      ["Preferences", "Units and theme", "Not started"],
      ["Accessibility pass", "Content descriptions, contrast, font scaling", "Not started"],
      ["Tests", "Parsing fixtures, Espresso flows, manual matrix", "Not started"],
      ["Attribution", "Visible credit with link", "Not started"]],
     [1.6, 3.5, 1.4], "checklist"),

    ("h1c", "Appendix D  Document control"),
    ("table", "Document information",
     ["Attribute", "Value"],
     [["Document", "Software Requirements Specification — WeatherApp"],
      ["Version", "1.0"],
      ["Status", "Draft for review"],
      ["Date", "22 November 2026"],
      ["Author", "Ahmad Hassan"],
      ["Source baseline", "github.com/ahmadhss9/WeatherApp at commit e0b1c03"],
      ["Supersedes", "None — first issue"],
      ["Review cycle", "Reviewed at milestone M1; reissued before M4"]],
     [1.7, 4.8], "docinfo"),
    ("table", "Approvals",
     ["Role", "Name", "Signature", "Date"],
     [["Author", "Ahmad Hassan", "", ""],
      ["Reviewer / supervisor", "", "", ""],
      ["Accepted by", "", "", ""]],
     [1.8, 2.1, 1.6, 1.0], "approvals"),
]
