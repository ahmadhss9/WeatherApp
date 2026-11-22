# -*- coding: utf-8 -*-
"""Content of SRS.docx as a list of render blocks.

Block kinds:
    ("h1"|"h2"|"h3", text)
    ("p", text)
    ("bullets", [text, ...])
    ("numbered", [text, ...])
    ("table", caption, headers, rows, widths)
    ("fig", filename, caption, width_in)
    ("code", text)
    ("note", text)
    ("pagebreak", None)
"""

DATE = "22 November 2026"
VERSION = "1.0"

# --------------------------------------------------------------------------
# 1. INTRODUCTION
# --------------------------------------------------------------------------
INTRO = [
    ("h1", "1  Introduction"),

    ("h2", "1.1  Purpose"),
    ("p", "This document specifies the software requirements for WeatherApp, an Android application that "
          "reports current weather conditions and short-range forecasts for the user's location or for a city "
          "the user selects. It records what the application must do, the quality attributes it must meet, the "
          "interfaces it exposes, and the rules that will be used to decide whether an implementation is "
          "acceptable. It is the reference for design, implementation, testing and acceptance of release 1.0, "
          "and the baseline against which later changes are assessed."),
    ("p", "The specification follows the structure recommended by ISO/IEC/IEEE 29148 for a software "
          "requirements specification: product perspective, functional requirements organised by feature, "
          "external interfaces, quality (non-functional) requirements, data definitions, verification, and "
          "traceability from requirement to design element and test case."),

    ("h2", "1.2  Scope"),
    ("p", "WeatherApp is a single-screen, offline-tolerant Android client for a hosted weather service. When it "
          "opens, it determines the user's location (with permission), requests a forecast, and presents "
          "current conditions, a set of supporting measurements, an hourly strip for the next 24 hours and a "
          "daily summary for up to seven days. Data is obtained from the Open-Meteo forecast API over HTTPS and "
          "is parsed into a typed Java model before being bound to the screen."),
    ("p", "The application is developed in Android Studio with Java 11 and XML layouts, targets Android "
          "devices from API 24 (Android 7.0) upward, and is packaged as a single APK. There is no proprietary "
          "server component: the client talks directly to the public weather API and needs no API key, no user "
          "account and no personal data beyond an optional location fix."),
    ("fig", "fig_context.png", "System context: the application, its user, the location provider and the external "
            "forecast service", 6.1, "context"),
    ("p", "Table {t:scope} draws the boundary of this specification. Items marked in scope are the subject of the "
          "requirements in Section 3; items out of scope are stated so that reviewers do not infer commitments "
          "that the project does not make."),
    ("table", "Scope of release 1.0",
     ["In scope", "Out of scope"],
     [["Current conditions for the active location",
       "User accounts, sign-in and server-side synchronisation"],
      ["Supporting measurements: feels-like, humidity, wind, precipitation",
       "Radar imagery, satellite layers and animated maps"],
      ["Hourly forecast for the next 24 hours",
       "Air-quality, pollen and UV-index reporting"],
      ["Daily forecast for up to 7 days",
       "Home-screen widgets, wearables and Android Auto"],
      ["Location detection with runtime permission",
       "Push notifications and severe-weather alerts"],
      ["City search with disambiguation",
       "Social sharing, ratings and in-app purchases"],
      ["Manual refresh and a cached last-known result",
       "A dedicated back-end service or analytics platform"],
      ["Metric/imperial units and light/dark appearance",
       "iOS, web or desktop clients"]],
     [3.25, 3.25], "scope"),

    ("h2", "1.3  Intended audience and reading suggestions"),
    ("p", "The document serves several audiences, each of which needs a different subset of it. Table {t:aud} "
          "summarises who should read what."),
    ("table", "Audiences and reading guidance",
     ["Reader", "Relevant sections", "Why"],
     [["Developer (author)", "All", "Implements the requirements and maintains this document"],
      ["Project supervisor / reviewer", "1, 2, 3, 5, 9, 10, 11",
       "Checks completeness, testability and coverage of the requirement set"],
      ["Test engineer", "3, 4, 5, 9, 10",
       "Derives test cases and acceptance criteria from requirement identifiers"],
      ["Maintainer of a later release", "2, 6, 7, 12",
       "Understands the data model, architecture and the deferred scope"],
      ["Stakeholder / end user representative", "1, 2.2, 4.1",
       "Confirms that the intended behaviour matches expectations"]],
     [1.75, 1.85, 2.9], "aud"),

    ("h2", "1.4  Definitions, acronyms and abbreviations"),
    ("table", "Definitions and abbreviations",
     ["Term", "Meaning"],
     [["API", "Application Programming Interface — here, the HTTP/JSON interface of the weather service"],
      ["APK", "Android Package Kit — the installable application file"],
      ["Fused location provider",
       "Google Play Services location client that merges GPS, Wi-Fi and cell signals into one fix"],
      ["Forecast horizon", "The period covered by a forecast request; 1 day for current conditions, 7 days for the daily view"],
      ["Gson", "Java library that maps JSON documents to objects and back"],
      ["JSON", "JavaScript Object Notation — the wire format of the weather response"],
      ["MoSCoW", "Prioritisation scheme: Must have, Should have, Could have, Won't have (this release)"],
      ["NFR", "Non-functional requirement — a quality attribute rather than a behaviour"],
      ["Retrofit", "Type-safe HTTP client for Android/Java that turns an annotated interface into API calls"],
      ["SRS", "Software Requirements Specification — this document"],
      ["WMO code", "Numeric weather-condition code defined by the World Meteorological Organization"],
      ["UTC / ISO 8601", "Coordinated Universal Time and its textual representation, e.g. 2026-11-22T15:00"]],
     [1.55, 4.95], "defs"),

    ("h2", "1.5  References"),
    ("p", "The following documents and resources are referenced by this specification or constrain it."),
    ("table", "Referenced documents and resources",
     ["Ref.", "Title", "Use in this document"],
     [["[1]", "ISO/IEC/IEEE 29148:2018, Systems and software engineering — Life cycle processes — Requirements engineering",
       "Structure and quality criteria for requirement statements"],
      ["[2]", "IEEE 830-1998, Recommended Practice for Software Requirements Specifications",
       "Classic SRS outline followed by Sections 1–5"],
      ["[3]", "Open-Meteo API documentation, https://open-meteo.com/en/docs",
       "Endpoint, parameter and weather-code semantics (Section 6)"],
      ["[4]", "Open-Meteo terms of use (CC BY 4.0 data licence)",
       "Attribution obligation, NFR-30"],
      ["[5]", "Android Developers — Request runtime permissions; Fused location provider",
       "Permission model and location client behaviour (Section 3.2)"],
      ["[6]", "Square Retrofit 2.11 documentation", "HTTP client configuration (Section 4.3)"],
      ["[7]", "Material Design 3 guidelines", "Layout, type scale and contrast rules (Section 4.1)"],
      ["[8]", "WCAG 2.1 level AA", "Accessibility criteria referenced by NFR-18 and NFR-20"],
      ["[9]", "WeatherApp source repository, https://github.com/ahmadhss9/WeatherApp",
       "Reference implementation at commit e0b1c03"],
      ["[10]", "Project introduction note (weatherapp.docx)", "Background, objectives and intended features"]],
     [0.55, 3.35, 2.6], "refs"),

    ("h2", "1.6  Document conventions and structure"),
    ("p", "Requirements are identified as FR-nn (functional) and NFR-nn (non-functional); use cases as UC-nn "
          "and test cases as TC-nn. Priority uses the MoSCoW scale: Must (release-blocking), Should (expected, "
          "deferrable with agreement), Could (desirable), Won't (explicitly deferred, see Section 12). The word "
          "shall marks a binding requirement, should a recommendation, and may a permitted option."),
    ("p", "Sections 1 and 2 place the product in context. Section 3 defines behaviour feature by feature and "
          "ends with the consolidated functional requirement table. Section 4 covers interfaces, Section 5 the "
          "quality attributes, Section 6 the data and API contract, and Section 7 the software design that "
          "satisfies Sections 3–6. Sections 8–10 carry the use cases, the traceability matrix and the "
          "verification plan. Sections 11 and 12 cover planning, risk and deferred scope."),
]

# --------------------------------------------------------------------------
# 2. OVERALL DESCRIPTION
# --------------------------------------------------------------------------
OVERALL = [
    ("h1", "2  Overall description"),
    ("p", "This section describes the product at a level above individual requirements: what it is part of, "
          "what it does, who uses it, where it runs, and what constrains its construction."),

    ("h2", "2.1  Product perspective"),
    ("p", "WeatherApp is a self-contained client in a three-party arrangement: the user, the Android device "
          "(which supplies location and network access) and the public forecast service (which supplies weather "
          "data). The application owns presentation and orchestration; it holds no weather data of its own. "
          "This keeps the product small and removes the operational burden of running a server, at the cost of "
          "depending on a third-party service for every fresh reading."),
    ("p", "The device contributes three capabilities: a network stack for HTTPS, a location provider that "
          "combines GPS with network-based positioning, and local storage for preferences and the last cached "
          "result. Open-Meteo contributes a REST endpoint that accepts a coordinate pair and a list of "
          "requested variables and returns a JSON document; it requires no registration and no key, which is "
          "why it was chosen for a student-scale project."),
    ("p", "Two product decisions follow from this arrangement and shape every requirement that follows. First, "
          "the application must behave acceptably when the network or the API is unavailable, because it has no "
          "local source of fresh data. Second, it must be economical with requests, because the public service "
          "is shared and rate-limited; the design therefore caches results and refreshes only when data is "
          "older than a configured interval."),

    ("h2", "2.2  Product functions"),
    ("p", "Table {t:functions} summarises the functional areas. Each is expanded into numbered requirements in Section 3."),
    ("table", "Summary of product functions",
     ["ID", "Function", "Summary", "Status in current build"],
     [["F-01", "Location acquisition", "Obtain a coordinate pair automatically, with graceful fallbacks",
       "Partial — hard-coded coordinates"],
      ["F-02", "Current conditions", "Temperature, condition text, feels-like, high/low and icon",
       "Partial — values not yet bound"],
      ["F-03", "Supporting measurements", "Humidity, wind speed, precipitation", "Partial — layout only"],
      ["F-04", "Hourly forecast", "Next 24 hours in a horizontally scrolling strip", "Layout only"],
      ["F-05", "Daily forecast", "Up to seven daily summaries", "Layout only"],
      ["F-06", "City search", "Find and switch to another city by name", "Not started"],
      ["F-07", "Refresh and caching", "Manual refresh, stale-data policy, offline cache", "Not started"],
      ["F-08", "Condition mapping", "Map WMO codes and day/night flag to icon and text", "Not started"],
      ["F-09", "Error handling", "Communicate failures and offer recovery", "Not started"],
      ["F-10", "Preferences", "Unit system, appearance, last selected city", "Not started"],
      ["F-11", "Accessibility & localisation", "Descriptive labels, externalised strings, scalable text", "Partial"],
      ["F-12", "Legal & attribution", "Visible weather-data attribution", "Not started"]],
     [0.5, 1.45, 2.55, 2.0], "functions"),

    ("h2", "2.3  User classes and characteristics"),
    ("p", "WeatherApp has a single end-user class, but that class uses the product in two distinct modes: a "
          "quick check of the current situation, and a planning check of the coming days. A secondary class of "
          "maintainer uses the same artefact and cares about the design rather than the experience."),
    ("table", "User classes",
     ["Class", "Profile", "Frequency", "Key needs"],
     [["Casual user", "General public, all ages; wants a single number and a symbol",
       "Several times a day, 5–20 seconds", "Instant clarity, no configuration, works on a weak connection"],
      ["Planner", "Commuters, students, travellers; decides what to wear or whether to travel",
       "Daily, 30–60 seconds", "Hourly detail for today, daily trend, precipitation and wind"],
      ["Low-vision / motor-impaired user", "Uses large text or a screen reader",
       "As above", "Scalable text, 48 dp touch targets, meaningful descriptions"],
      ["Maintainer (developer)", "Builds and extends the application",
       "Continuous during the project", "Layered code, typed model, testable boundaries, clear requirement IDs"]],
     [1.5, 1.85, 1.5, 1.65], "users"),

    ("h2", "2.4  Operating environment"),
    ("table", "Operating environment",
     ["Attribute", "Value"],
     [["Platform", "Android, Java 11, single application module"],
      ["Minimum SDK", "API 24 (Android 7.0 Nougat)"],
      ["Target / compile SDK", "API 37"],
      ["Device classes", "Phones with a portrait screen of 4.7 in to 6.9 in; tablets render the phone layout"],
      ["Display", "Light and dark appearance; Material 3 dynamic colour where available"],
      ["Connectivity", "Wi-Fi, cellular or none; HTTPS to api.open-meteo.com"],
      ["Sensors and services", "Google Play Services location (fused provider); no other sensors required"],
      ["Build tooling", "Gradle with Kotlin DSL, Android Gradle Plugin, version catalog, JDK 11"],
      ["Languages", "English (default); further languages by resource overlay (Section 12)"]],
     [1.75, 4.75], "env"),

    ("h2", "2.5  Design and implementation constraints"),
    ("p", "The following constraints are imposed by the platform, the chosen service or the project itself and "
          "are not negotiable within release 1.0."),
    ("bullets", [
        "The client shall use only free, key-less public services; no paid weather provider, no billing account "
        "and no secret embedded in the APK.",
        "All network access shall use HTTPS; cleartext traffic shall not be enabled for the weather host.",
        "The application shall remain a single-activity application in release 1.0; additional screens, if "
        "introduced, shall be fragments or dialect screens rather than a navigation rewrite.",
        "Java 11 is the language level; Kotlin, Jetpack Compose and other language or UI stacks are outside the "
        "approved toolchain for this release.",
        "Location shall be requested at runtime only; no background location permission shall be declared.",
        "Android 12 and later may return only an approximate fix; the product shall tolerate a location "
        "accurate to a few kilometres.",
        "The build shall be reproducible from the repository with the Gradle wrapper and no local configuration "
        "file beyond android local.properties.",
        "The dataset from Open-Meteo is licensed CC BY 4.0, so attribution is a hard requirement, not a "
        "courtesy.",
    ]),

    ("h2", "2.6  Assumptions and dependencies"),
    ("table", "Assumptions and dependencies",
     ["ID", "Assumption or dependency", "Impact if untrue"],
     [["A-01", "The device has a working internet connection at least occasionally",
       "The application can show only cached data; freshness requirements fail"],
      ["A-02", "Google Play Services is present and reasonably current",
       "The fused provider is unavailable; the design falls back to the platform location manager"],
      ["A-03", "The user grants location permission, or accepts typing a city",
       "Without either, no location-specific forecast can be produced"],
      ["A-04", "Open-Meteo remains available and its response schema stays compatible",
       "The client shows an error state; a schema change requires a parser update"],
      ["A-05", "The device clock and time zone are correct",
       "Hourly labels and the day/night icon may be wrong"],
      ["A-06", "The APK is installed from a trusted source on a device with API 24 or higher",
       "Installation fails on older devices, which are out of scope"],
      ["A-07", "Weather variables requested by name (temperature_2m and peers) remain documented",
       "The request returns nulls and the UI shows placeholders"]],
     [0.6, 3.3, 2.6], "assume"),

    ("h2", "2.7  Implementation status at the time of writing"),
    ("p", "This specification describes release 1.0. The repository at commit e0b1c03 already contains a "
          "complete layout and a working network layer, but the screen still shows sample values. Table {t:status} "
          "records the honest position of each area; the column is repeated as Appendix C so that progress can "
          "be tracked against the same identifiers."),
    ("table", "Implementation status against this specification",
     ["Area", "Implemented", "Evidence"],
     [["Screen layout and visual design", "Complete",
       "activity_main.xml: header, current card, metric tiles, hourly strip, seven-day list"],
      ["Weather icons and theme", "Complete", "res/drawable set plus Material 3 DayNight theme"],
      ["HTTP client and endpoint interface", "Complete", "RetrofitClient, WeatherApi with @GET v1/forecast"],
      ["Response model", "Complete", "WeatherResponse and nested CurrentWeather with Gson annotations"],
      ["Live request", "Partial", "MainActivity issues one request with fixed coordinates and logs the result"],
      ["Binding results to the screen", "Not started", "Text views still hold preview values"],
      ["Location detection", "Not started", "Play Services dependency and permissions declared"],
      ["City search, caching, preferences", "Not started", "No code yet"],
      ["Automated tests", "Partial", "Instrumented and unit test stubs generated by the project template"]],
     [1.9, 1.0, 3.6], "status"),
]

# --------------------------------------------------------------------------
# 3. SYSTEM FEATURES
# --------------------------------------------------------------------------
FEATURES = [
    ("h1", "3  System features and functional requirements"),
    ("p", "This section describes each functional area as a system feature: what it does, the actors and "
          "preconditions involved, the normal and alternative flows, and the individual requirements that make "
          "it acceptable. Requirement identifiers are stable and are reused in the traceability matrix "
          "(Section 9) and the test plan (Section 10)."),
    ("h2", "3.1  Requirement structure and priorities"),
    ("p", "Every functional requirement states a single testable behaviour in the form \"the system shall "
          "\u2026\". Priorities follow MoSCoW: Must requirements are release-blocking, Should requirements are "
          "expected in release 1.0 but may be deferred with the reviewer's agreement, Could requirements are "
          "desirable and may be dropped, and Wont requirements are recorded as out of scope. Of the "
          "forty-three functional requirements, twenty-seven are Must, fourteen are Should and two are Could."),

    ("h2", "3.2  F-01 Location acquisition"),
    ("p", "WeatherApp must work without the user typing anything, which means it must turn the device position "
          "into a coordinate pair. It uses the fused location provider from Google Play Services, asking for a "
          "cached fix first and falling back to a fresh one, because the cheapest fix is usually adequate for a "
          "weather query: a kilometre of positional error changes the forecast very little. The permission is "
          "requested in context with a short explanation, never at cold start before the user knows why."),
    ("p", "Three failure paths matter. If permission is denied, the application must not dead-end: it offers "
          "the city search path instead and remembers the choice. If permission is granted but no fix arrives "
          "within a sensible wait, the application falls back to a network-derived location. If even that "
          "fails, the last known city remains on screen with a clear indication that it is not current."),
    ("p", "Precondition: the application is launched and no city has been pinned by the user. Trigger: launch "
          "or an explicit location action. Postcondition: an active coordinate pair, a locality name, or a "
          "recorded failure with a recovery path."),
    ("bullets", [
        "Request ACCESS_COARSE_LOCATION and ACCESS_FINE_LOCATION at runtime, on first launch and on demand.",
        "Use the fused provider: last known location first, then a single high-accuracy update.",
        "Cap the wait at five seconds, then fall back.",
        "Reverse-geocode the fix to a city or locality label for the header.",
        "Treat approximate fixes (Android 12 coarse-only) as acceptable.",
        "Never request background location.",
    ]),
    ("p", "Acceptance criteria: with permission granted, a coordinate pair is obtained and shown as a locality "
          "name within five seconds on a typical device, and the forecast on screen corresponds to that "
          "locality; with permission denied, the city search path is offered in the same launch."),

    ("h2", "3.3  F-02 Current conditions"),
    ("p", "The current-conditions card is the centre of the screen and answers the question the user came "
          "with: how warm is it, and what does it look like outside. It shows a large temperature, a short "
          "condition phrase, the apparent temperature and, when available, today's high and low so that the "
          "single number has context. The illustration changes with the condition and with the day/night flag, "
          "because a moon at noon reads as a bug."),
    ("p", "Values arrive as numbers in the API's units: degrees Celsius, metres per second for wind and "
          "millimetres for precipitation. The client converts wind to kilometres per hour and rounds "
          "temperature to a whole number; decimals imply a precision the source does not have."),
    ("bullets", [
        "Request current temperature, humidity, apparent temperature, day flag, precipitation, weather code "
        "and ten-metre wind speed in a single call.",
        "Render temperature rounded to the nearest whole degree with the degree symbol.",
        "Render the condition phrase from the weather code (Section 6.3) and the icon from code plus day flag.",
        "Render apparent temperature as \"Feels like N degrees\".",
        "Render today's minimum and maximum when the daily block is present.",
        "Show the observation time of the reading so a stale screen is recognisable.",
    ]),

    ("h2", "3.4  F-03 Supporting measurements"),
    ("p", "Below the main card, two tiles give the numbers that decide comfort: humidity and wind. They are "
          "deliberately small and equal in weight, because users scan them rather than read them. Precipitation "
          "is shown only when it is non-zero, to avoid a row of zeros on a dry day."),
    ("bullets", [
        "Show relative humidity as a whole percentage.",
        "Show wind speed in kilometres per hour, converted from metres per second.",
        "Show precipitation in millimetres only when greater than zero.",
        "Keep both tiles visible without scrolling on a 5.5-inch screen.",
    ]),

    ("h2", "3.5  F-04 Hourly forecast"),
    ("p", "The hourly strip extends the answer beyond now. Twenty-four entries are enough to cover the rest of "
          "today and the coming night, which is the horizon that changes behaviour. The strip scrolls "
          "horizontally, the current hour is highlighted, and each entry carries an hour label, an icon and a "
          "temperature."),
    ("bullets", [
        "Request the hourly variables temperature, precipitation probability and weather code for 24 hours.",
        "Render one chip per hour with a local-time label and a temperature.",
        "Highlight the chip representing the current hour.",
        "Use a 12- or 24-hour clock consistent with the device locale.",
        "Preserve scroll position across a configuration change.",
    ]),

    ("h2", "3.6  F-05 Daily forecast"),
    ("p", "The seven-day list answers the planning question. Each row carries the day name, a representative "
          "icon and the minimum and maximum temperature; today is labelled explicitly rather than by a date, "
          "and the row for today is emphasised."),
    ("bullets", [
        "Request daily maximum and minimum temperature, precipitation sum and weather code for seven days.",
        "Render one row per day with day name, icon and \"max / min\".",
        "Label the first row \"Today\" rather than repeating the current day name.",
        "Keep the list inside the existing container; do not introduce a second scrolling surface.",
    ]),

    ("h2", "3.7  F-06 City search"),
    ("p", "Location is a convenience, not a requirement: the application must also answer for a city the user "
          "names, whether because permission was denied, because they are planning a trip, or because the fix "
          "is wrong. Search uses the geocoding endpoint of the same service, requires at least two characters, "
          "and presents matches with region and country so that two Springfields can be told apart."),
    ("bullets", [
        "Provide a search action reachable from the header in at most one tap.",
        "Query the geocoding endpoint after at least two characters, debounced to one request per 400 ms.",
        "Present at least the top five matches with name, administrative area and country.",
        "On selection, fetch weather for the chosen coordinates and show the chosen city in the header.",
        "Persist the last selected city and restore it on the next launch.",
    ]),

    ("h2", "3.8  F-07 Refresh, caching and staleness"),
    ("p", "A weather screen that never updates is worse than one that says when it last updated. The "
          "application refreshes on pull, on return to the foreground when the cached result is older than "
          "fifteen minutes, and after a successful location change. Results are cached so that a later launch "
          "without connectivity can still show the last known state, marked as such."),
    ("bullets", [
        "Refresh on pull-to-refresh gesture with a visible progress indicator.",
        "Refresh automatically when the screen returns to the foreground and cached data is older than 15 minutes.",
        "Cache the most recent successful response per location for up to 30 minutes.",
        "Show the age of the displayed data when it is older than 15 minutes.",
        "Cancel an in-flight request when a newer one starts, and ignore out-of-order responses.",
    ]),

    ("h2", "3.9  F-08 Condition mapping"),
    ("p", "The API reports conditions as WMO codes, which are precise but not readable. The client maps each "
          "code to a phrase and to one of the illustration families already present in the project: clear, "
          "partly cloudy, cloudy, rain, snow and thunder. The day/night flag selects the night variant where "
          "one exists."),
    ("bullets", [
        "Map every code listed in Section 6.3 to a non-empty phrase and an icon.",
        "Select a night variant for clear and partly cloudy conditions when the day flag is zero.",
        "Fall back to a neutral icon and the phrase \"Unknown\" for unrecognised codes rather than crashing.",
        "Use the same mapping for current, hourly and daily renderings.",
    ]),

    ("h2", "3.10  F-09 Error handling and degraded operation"),
    ("p", "Failures are normal on a mobile client: the tunnel, the lift, the aeroplane. The application must "
          "distinguish a wrong location from a slow network from a broken service, and must say which one it "
          "is. Every error state offers a way forward; none is a dead end."),
    ("bullets", [
        "Show a distinct message for no connectivity, timeout, HTTP error and unparsable response.",
        "Offer a retry action that reissues the last request without duplicating it.",
        "Keep previously displayed data visible when a refresh fails, marked as stale.",
        "Never silently replace data with empty values.",
        "Log unexpected failures with the request identity and HTTP status for diagnosis.",
    ]),

    ("h2", "3.11  F-10 Preferences and appearance"),
    ("p", "Two preferences are worth their complexity in release 1.0: units and appearance. The unit setting "
          "changes displayed values without changing the network request, which always uses metric units. "
          "Appearance follows the system by default, with a manual override for users who prefer a fixed theme."),
    ("bullets", [
        "Offer metric (degrees Celsius, kilometres per hour) and imperial (degrees Fahrenheit, miles per hour) display.",
        "Convert at render time; keep the request and the cache in metric units.",
        "Follow the system light/dark setting by default and allow an explicit override.",
        "Persist both preferences and apply them without a restart.",
    ]),

    ("h2", "3.12  F-11 Accessibility, localisation and attribution"),
    ("p", "Accessibility is a requirement rather than a polish item: the main card carries colour alone to "
          "convey little, but the forecast strip and its icons can only be understood through text. All "
          "user-visible strings move to resources, every image gets a description that includes the condition, "
          "and the weather service is credited where the data appears."),
    ("bullets", [
        "Provide a content description for every weather illustration, naming the condition.",
        "Keep text contrast at or above 4.5:1 for body text and 3:1 for large text.",
        "Support font scaling to 200 per cent without clipping or truncation of essential values.",
        "Externalise all user-visible strings; no hard-coded text in layouts or Java.",
        "Display \"Weather data by Open-Meteo.com\" with a link, as the data licence requires.",
    ]),

    ("h2", "3.13  Consolidated functional requirements"),
    ("p", "Table {t:fr} is the authoritative list. Identifiers are grouped by feature and are referenced by the "
          "design (Section 7), the use cases (Section 8) and the tests (Section 10)."),
    ("table", "Functional requirements",
     ["ID", "Requirement", "Priority"],
     [["F-01 Location acquisition", "", ""],
      ["FR-01", "The system shall request location permission at runtime, with a rationale, when a location is first needed.", "Must"],
      ["FR-02", "The system shall obtain a coordinate pair from the fused location provider, using a cached fix when one is available.", "Must"],
      ["FR-03", "The system shall fall back to city search when location permission is denied.", "Must"],
      ["FR-04", "The system shall fall back to network-derived location when no satellite fix is available within five seconds.", "Should"],
      ["FR-05", "The system shall reverse-geocode the active coordinates to a locality label for display.", "Should"],
      ["FR-06", "The system shall treat an approximate location fix as valid.", "Must"],
      ["F-02 Current conditions", "", ""],
      ["FR-07", "The system shall request current conditions for the active location on launch and on refresh.", "Must"],
      ["FR-08", "The system shall display temperature rounded to the nearest whole degree.", "Must"],
      ["FR-09", "The system shall display a condition phrase derived from the weather code.", "Must"],
      ["FR-10", "The system shall display apparent temperature.", "Must"],
      ["FR-11", "The system shall display the day's maximum and minimum temperature when available.", "Should"],
      ["FR-12", "The system shall display the observation time of the shown reading.", "Should"],
      ["F-03 Supporting measurements", "", ""],
      ["FR-13", "The system shall display relative humidity as a whole percentage.", "Must"],
      ["FR-14", "The system shall display wind speed in kilometres per hour, converted from metres per second.", "Must"],
      ["FR-15", "The system shall display precipitation in millimetres only when greater than zero.", "Should"],
      ["F-04 Hourly forecast", "", ""],
      ["FR-16", "The system shall request hourly data covering the next 24 hours.", "Must"],
      ["FR-17", "The system shall render one entry per hour with a local-time label, icon and temperature.", "Must"],
      ["FR-18", "The system shall highlight the entry for the current hour.", "Should"],
      ["FR-19", "The system shall present hourly entries in a horizontally scrolling strip.", "Must"],
      ["F-05 Daily forecast", "", ""],
      ["FR-20", "The system shall request daily data for seven days.", "Must"],
      ["FR-21", "The system shall render one row per day with day name, icon and maximum/minimum temperature.", "Must"],
      ["FR-22", "The system shall label the first daily row \"Today\".", "Could"],
      ["F-06 City search", "", ""],
      ["FR-23", "The system shall provide a search action reachable in at most one tap from the main screen.", "Must"],
      ["FR-24", "The system shall query the geocoding endpoint after at least two characters, debounced to 400 ms.", "Should"],
      ["FR-25", "The system shall show at least five matches with name, region and country.", "Must"],
      ["FR-26", "The system shall fetch weather for the selected city and label it in the header.", "Must"],
      ["FR-27", "The system shall persist the last selected city across launches.", "Should"],
      ["F-07 Refresh and caching", "", ""],
      ["FR-28", "The system shall support pull-to-refresh with a visible progress indicator.", "Must"],
      ["FR-29", "The system shall refresh automatically when the screen resumes and cached data is older than 15 minutes.", "Should"],
      ["FR-30", "The system shall cache the last successful response per location for up to 30 minutes.", "Should"],
      ["FR-31", "The system shall show the age of displayed data when it is older than 15 minutes.", "Should"],
      ["FR-32", "The system shall cancel or ignore superseded in-flight requests.", "Could"],
      ["F-08 Condition mapping", "", ""],
      ["FR-33", "The system shall map every supported weather code to a phrase and an icon.", "Must"],
      ["FR-34", "The system shall select a night icon for clear and partly cloudy conditions when the day flag is zero.", "Must"],
      ["FR-35", "The system shall degrade to a neutral icon and \"Unknown\" for unrecognised codes.", "Should"],
      ["F-09 Error handling", "", ""],
      ["FR-36", "The system shall distinguish no connectivity, timeout, HTTP error and parse failure in its messages.", "Must"],
      ["FR-37", "The system shall offer a retry action for recoverable failures.", "Must"],
      ["FR-38", "The system shall retain stale data on a failed refresh and mark it as stale.", "Must"],
      ["F-10 Preferences", "", ""],
      ["FR-39", "The system shall offer metric and imperial display units, converting at render time.", "Should"],
      ["FR-40", "The system shall follow the system appearance and allow an explicit override.", "Should"],
      ["F-11 Accessibility and attribution", "", ""],
      ["FR-41", "The system shall provide content descriptions naming the condition for every weather illustration.", "Must"],
      ["FR-42", "The system shall externalise all user-visible strings to resources.", "Must"],
      ["FR-43", "The system shall display attribution to Open-Meteo wherever weather data is shown.", "Must"]],
     [0.62, 4.93, 0.95], "fr"),
]
