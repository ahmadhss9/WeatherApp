"""Generates every figure used in SRS.docx."""
import os

from figlib import (AMBER, AMBER_SOFT, BLUE, BLUE_MID, BLUE_SOFT, Canvas, GREY,
                    GREY_LIGHT, INK, LINE, NAVY, PANEL, RED, RED_SOFT, TEAL,
                    TEAL_SOFT, WHITE)

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "figures")
os.makedirs(OUT, exist_ok=True)


# ----------------------------------------------------------------- helpers
def sun(c, x, y, r=26, color="#FFC55C"):
    import math
    for i in range(8):
        a = math.radians(i * 45)
        c.line([(x + math.cos(a) * r * 1.25, y + math.sin(a) * r * 1.25),
                (x + math.cos(a) * r * 1.62, y + math.sin(a) * r * 1.62)],
               color=color, width=max(1.0, r * 0.17))
    c.ellipse(x - r, y - r, 2 * r, 2 * r, fill=color)


def cloud(c, x, y, s=1.0, color="#FFFFFF", shade="#E3ECFB"):
    c.ellipse(x - 26 * s, y - 6 * s, 34 * s, 26 * s, fill=color)
    c.ellipse(x - 4 * s, y - 16 * s, 38 * s, 32 * s, fill=color)
    c.ellipse(x + 16 * s, y - 2 * s, 30 * s, 24 * s, fill=color)
    c.rect(x - 26 * s, y + 4 * s, 72 * s, 12 * s, r=6 * s, fill=color)
    c.rect(x - 30 * s, y + 13 * s, 76 * s, 5 * s, r=2.5 * s, fill=shade)


def badge(c, x, y, n, r=11, fill=BLUE, color=WHITE):
    c.ellipse(x - r, y - r, 2 * r, 2 * r, fill=fill)
    c.text(x, y - 0.5, str(n), size=r * 1.2, weight="bold", color=color)


def poly_arrow(c, pts, color=GREY, width=1.4, head=7, dash=False):
    for a, b in zip(pts, pts[1:-1]):
        c.line([a, b], color=color, width=width)
    c.arrow(pts[-2], pts[-1], color=color, width=width, head=head, dash=dash)


# ----------------------------------------------------------------- figure 1
def fig_context():
    c = Canvas(1000, 520)
    c.rect(355, 125, 290, 130, r=14, fill=WHITE, outline=NAVY, width=2, shadow=True)
    c.text(500, 157, "WeatherApp", size=25, weight="bold", color=NAVY)
    c.text(500, 185, "Android application", size=14, color=GREY)
    c.text(500, 207, "com.example.weatherapp  ·  v1.0", size=11.5, color=GREY_LIGHT)
    c.text(500, 229, "Java 11  ·  API 24–37", size=11.5, color=GREY_LIGHT)

    c.actor(150, 170, s=1.05, color=INK)
    c.text(150, 228, "User", size=17, weight="bold", color=INK)
    c.text(150, 248, "«actor»", size=11, color=GREY_LIGHT)

    c.rect(775, 120, 200, 140, r=12, fill=PANEL, outline=TEAL, width=1.6, shadow=True)
    c.text(875, 149, "Open-Meteo", size=17, weight="bold", color=TEAL)
    c.text(875, 170, "REST forecast API", size=12, color=GREY)
    c.text(875, 197, "api.open-meteo.com", size=11.5, color=GREY)
    c.text(875, 216, "/v1/forecast", size=11.5, color=GREY)
    c.text(875, 240, "no API key required", size=10.5, color=TEAL)

    c.rect(330, 340, 260, 100, r=12, fill=WHITE, outline=BLUE, width=1.5)
    c.text(460, 365, "Location Provider", size=15, weight="bold", color=BLUE)
    c.text(460, 388, "Google Play Services · Fused", size=11.5, color=GREY)
    c.text(460, 409, "ACCESS_FINE / COARSE_LOCATION", size=10.5, color=GREY_LIGHT)

    c.rect(700, 340, 235, 100, r=12, fill=WHITE, outline=GREY_LIGHT, width=1.4, dash=True)
    c.text(817, 365, "Device storage", size=15, weight="bold", color=GREY)
    c.text(817, 388, "SharedPreferences cache", size=11.5, color=GREY)
    c.text(817, 409, "offline reads (planned)", size=10.5, color=GREY_LIGHT)

    c.arrow((196, 163), (348, 163), color=INK, width=1.6, head=8,
            label="opens app, views forecast", lpos=0.5, loff=(0, -14), lsize=11.5)
    c.arrow((348, 215), (196, 215), color=INK, width=1.4, head=8, dash=True,
            label="rendered weather screen", lpos=0.5, loff=(0, -13), lsize=11.5)
    c.arrow((652, 163), (768, 163), color=TEAL, width=1.6, head=8,
            label="GET /v1/forecast", lpos=0.5, loff=(0, -14), lsize=11)
    c.arrow((768, 215), (652, 215), color=TEAL, width=1.4, head=8, dash=True,
            label="JSON response", lpos=0.5, loff=(0, -13), lsize=11.5)
    c.arrow((460, 333), (460, 263), color=BLUE, width=1.5, head=8,
            label="lat / lon", lpos=0.5, loff=(44, 1), lsize=11.5)
    c.arrow((620, 257), (740, 335), color=GREY_LIGHT, width=1.4, head=8, dash=True,
            label="cache (planned)", lpos=0.44, loff=(26, -12), lsize=11, lcolor=GREY)

    c.text(500, 492, "Solid lines: implemented in the current build   ·   Dashed lines: planned for release 1.0",
           size=11.5, color=GREY_LIGHT)
    return c.save(os.path.join(OUT, "fig_context.png"))


# ----------------------------------------------------------------- figure 2
def fig_arch():
    c = Canvas(1000, 660)
    bands = [
        ("PRESENTATION", 40, BLUE_SOFT, BLUE,
         [("MainActivity.java", "onCreate() · binds views · triggers request", 226),
          ("activity_main.xml", "ScrollView + cards, tiles, forecast strips", 246),
          ("res/drawable · values", "weather icons, colours, themes", 222)]),
        ("NETWORKING", 190, TEAL_SOFT, TEAL,
         [("RetrofitClient", "singleton · BASE_URL · Gson converter", 226),
          ("WeatherApi «interface»", "@GET /v1/forecast + @Query params", 246),
          ("Gson / OkHttp", "JSON ↔ Java, HTTP transport", 222)]),
        ("MODEL", 340, AMBER_SOFT, AMBER,
         [("WeatherResponse", "latitude, longitude, timezone", 226),
          ("CurrentWeather", "temperature, humidity, wind, weather_code", 246),
          ("Gson @SerializedName", "field ↔ JSON key mapping", 222)]),
        ("PLATFORM", 490, "#EEF1F6", GREY,
         [("Android runtime", "API 24 (min) → 37 (target), Java 11", 226),
          ("Play Services Location", "FusedLocationProviderClient", 246),
          ("Permissions", "INTERNET · FINE/COARSE location", 222)]),
    ]
    for name, y, fill, stroke, boxes in bands:
        c.rect(20, y, 700, 108, r=12, fill=fill, outline=stroke, width=1.1)
        c.text(34, y + 17, name, size=10.5, weight="bold", color=stroke, anchor="lm")
        x = 34
        for title, sub, w in boxes:
            c.rect(x, y + 28, w - 12, 64, r=8, fill=WHITE, outline=stroke, width=1.0, shadow=True)
            c.text(x + 12, y + 47, title, size=12.3, weight="bold", color=INK, anchor="lm")
            c.para(x + 12, y + 64, w - 34, sub, size=10.2, color=GREY, spacing=1.22)
            x += w

    c.rect(772, 190, 205, 108, r=12, fill=WHITE, outline=TEAL, width=1.7, shadow=True)
    c.text(874, 214, "Open-Meteo API", size=14, weight="bold", color=TEAL)
    c.text(874, 236, "https://api.open-meteo.com", size=10.5, color=GREY)
    c.text(874, 256, "GET /v1/forecast", size=10.5, color=GREY)
    c.text(874, 276, "JSON, no key", size=10.5, color=GREY)

    for y1, y2, label in ((148, 190, "calls"), (298, 340, "deserialises"), (448, 490, "runs on")):
        c.arrow((360, y1), (360, y2 - 2), color=GREY, width=1.4, head=7,
                label=label, lpos=0.5, loff=(34, 0), lsize=10.5, lcolor=GREY)
    c.arrow((722, 236), (764, 236), color=TEAL, width=1.6, head=8,
            label="HTTPS", lpos=0.5, loff=(0, -12), lsize=10)

    c.text(22, 626, "Layers are ordered top-down by dependency; every element shown here exists in the repository.",
           size=10.5, color=GREY_LIGHT, anchor="lm")
    return c.save(os.path.join(OUT, "fig_arch.png"))


# ----------------------------------------------------------------- figure 3
def fig_usecase():
    c = Canvas(1000, 710)
    c.rect(240, 36, 520, 620, r=14, fill="#FBFCFE", outline=NAVY, width=1.4)
    c.text(254, 54, "WeatherApp  —  system boundary", size=11.5, weight="bold", color=NAVY, anchor="lm")

    def uc(cx, cy, w, h, title, sub, fill=WHITE, stroke=NAVY):
        c.ellipse(cx - w / 2, cy - h / 2, w, h, fill=fill, outline=stroke, width=1.3)
        c.text(cx, cy - 7, title, size=12.5, weight="bold", color=INK)
        c.text(cx, cy + 11, sub, size=10, color=GREY_LIGHT)

    uc(380, 120, 230, 62, "View current weather", "UC-01")
    uc(380, 215, 230, 62, "View hourly forecast", "UC-02")
    uc(380, 310, 230, 62, "View 7-day forecast", "UC-03")
    uc(380, 405, 230, 62, "Refresh weather data", "UC-04")
    uc(380, 500, 230, 62, "Search city", "UC-05")
    uc(380, 595, 230, 62, "Change units / theme", "UC-06")
    uc(650, 120, 200, 62, "Detect location", "UC-07", fill=BLUE_SOFT)
    uc(650, 310, 200, 62, "Fetch forecast", "UC-08", fill=BLUE_SOFT)
    uc(650, 500, 200, 62, "Show error state", "UC-09", fill=RED_SOFT, stroke=RED)

    c.actor(120, 330, s=1.05, color=INK)
    c.text(120, 388, "User", size=17, weight="bold", color=INK)
    for cy in (120, 215, 310, 405, 500, 595):
        c.line([(138, 330), (263, cy)], color=GREY, width=1.1)

    c.arrow((495, 120), (548, 120), color=BLUE, width=1.2, head=6, dash=True,
            label="«include»", lpos=0.5, loff=(0, -12), lsize=9.5)
    c.arrow((650, 152), (650, 278), color=BLUE, width=1.2, head=6, dash=True,
            label="«include»", lpos=0.5, loff=(48, 0), lsize=9.5)
    c.arrow((495, 405), (548, 330), color=BLUE, width=1.2, head=6, dash=True,
            label="«include»", lpos=0.5, loff=(18, 14), lsize=9.5)
    c.arrow((650, 343), (650, 468), color=RED, width=1.2, head=6, dash=True,
            label="«extend» [error]", lpos=0.5, loff=(55, 0), lsize=9.5, lcolor=RED)
    c.arrow((750, 310), (818, 310), color=TEAL, width=1.4, head=6,
            label="HTTPS", lpos=0.5, loff=(0, -11), lsize=9.5, lcolor=TEAL)
    c.rect(818, 274, 152, 72, r=10, fill=WHITE, outline=TEAL, width=1.3)
    c.text(894, 296, "«system»", size=9.5, color=GREY)
    c.text(894, 316, "Open-Meteo", size=12.5, weight="bold", color=TEAL)
    c.text(894, 333, "forecast API", size=10, color=GREY)

    c.text(240, 686, "Grey lines: user associations.   Blue dashed: «include».   Red dashed: «extend» for the failure path.",
           size=10.5, color=GREY_LIGHT, anchor="lm")
    return c.save(os.path.join(OUT, "fig_usecase.png"))


# ----------------------------------------------------------------- figure 4
def fig_sequence():
    c = Canvas(1080, 790)
    lanes = [("User", 90), ("MainActivity", 252), ("LocationProvider", 414),
             ("WeatherApi", 576), ("Open-Meteo", 738), ("UI views", 900)]
    L = dict(lanes)
    top, h = 46, 44
    for name, x in lanes:
        c.rect(x - 65, top, 130, h, r=8, fill=NAVY, outline=NAVY, width=1.0)
        c.text(x, top + h / 2 - 1, name, size=11, weight="semibold", color=WHITE)
        c.line([(x, top + h), (x, 752)], color=LINE, width=1.0, dash=(6, 5))

    def act(x, y1, y2):
        c.rect(x - 7, y1, 14, y2 - y1, r=2, fill=BLUE_MID, outline=BLUE, width=0.8)

    def msg(a, b, y, label, dash=False, color=GREY, size=10.5, loff=-10):
        c.arrow((L[a] + 8, y), (L[b] - 8, y), color=color, width=1.35, head=7, dash=dash,
                label=label, lpos=0.5, loff=(0, loff), lsize=size, lcolor=INK)

    msg("User", "MainActivity", 132, "launch application")
    msg("MainActivity", "LocationProvider", 170, "request location (permission)", color=BLUE)
    msg("LocationProvider", "MainActivity", 208, "lat, lon", dash=True)
    msg("MainActivity", "WeatherApi", 258, "getWeather(lat, lon, current fields)", color=BLUE)
    msg("WeatherApi", "Open-Meteo", 298, "GET /v1/forecast?…", color=BLUE)
    msg("Open-Meteo", "WeatherApi", 336, "200 OK · JSON", dash=True, color=TEAL)
    msg("WeatherApi", "MainActivity", 374, "WeatherResponse (Gson)", dash=True, color=TEAL)

    act(252, 132, 640)
    act(576, 258, 390)
    act(738, 298, 344)

    c.rect(40, 412, 1000, 228, r=6, fill=None, outline=GREY, width=1.0)
    c.rect(40, 412, 88, 26, r=4, fill="#EDF1F7", outline=GREY, width=0.8)
    c.text(84, 426, "alt", size=11, weight="bold", color=INK)
    c.text(138, 426, "[response.isSuccessful()]", size=10, color=GREY, anchor="lm")
    c.text(138, 456, "[success]", size=10, color=TEAL, anchor="lm")

    msg("MainActivity", "UI views", 486, "tvTemperature.text = \"28°\"  (and the other bound views)", color=BLUE, size=9.8)
    msg("MainActivity", "UI views", 514, "set icon from weather_code + is_day", color=BLUE, size=9.8)

    c.line([(40, 538), (1040, 538)], color=LINE, width=0.8)
    c.text(138, 556, "[failure / timeout]", size=10, color=RED, anchor="lm")
    msg("MainActivity", "UI views", 582, "show error, keep last data (planned)", dash=True, color=RED, size=9.8)

    c.rect(120, 652, 880, 74, r=4, fill=WHITE, outline=GREY_LIGHT, width=1.0)
    c.rect(120, 652, 76, 22, r=4, fill="#EDF1F7", outline=GREY_LIGHT, width=0.8)
    c.text(158, 663, "note", size=9.5, weight="bold", color=GREY)
    c.para(134, 690, 850,
           "The current build writes the outcome to Logcat (\"WEATHER_TEST\") instead of updating the screen; "
           "binding the response to the views is the next implementation step.",
           size=9.5, color=GREY, spacing=1.3)

    c.text(40, 766, "Sequence for UC-01 “View current weather”. Lifelines follow the package structure of the repository.",
           size=10.5, color=GREY_LIGHT, anchor="lm")
    return c.save(os.path.join(OUT, "fig_sequence.png"))


# ----------------------------------------------------------------- figure 5
def flow_node(c, cx, cy, w, h, text, fill=WHITE, stroke=NAVY, size=12.3, sub=None):
    c.rect(cx - w / 2, cy - h / 2, w, h, r=9, fill=fill, outline=stroke, width=1.4, shadow=True)
    if sub:
        c.text(cx, cy - 6, text, size=size, weight="bold", color=INK)
        c.text(cx, cy + 11, sub, size=9.5, color=GREY)
    else:
        c.text(cx, cy, text, size=size, weight="semibold", color=INK)


def diamond(c, cx, cy, w, h, text, fill="#FFF9EC", stroke=AMBER):
    c.polygon([(cx, cy - h / 2), (cx + w / 2, cy), (cx, cy + h / 2), (cx - w / 2, cy)],
              fill=fill, outline=stroke)
    c.para(cx - w / 2 + 26, cy, w - 52, text, size=11, color=INK, anchor="m", align="center")


def fig_flow():
    c = Canvas(1000, 1120)
    cx = 460
    c.ellipse(cx - 13, 22, 26, 26, fill=INK)
    flow_node(c, cx, 88, 320, 46, "Launch MainActivity", fill=BLUE_SOFT, stroke=BLUE)
    c.arrow((cx, 48), (cx, 64), color=INK, width=1.5, head=7)

    c.arrow((cx, 111), (cx, 158), color=INK, width=1.5, head=7)
    diamond(c, cx, 215, 330, 112, "Runtime location permission granted?")
    c.text(cx + 44, 292, "yes", size=10.5, color=TEAL, anchor="lm")
    c.arrow((cx, 271), (cx, 318), color=INK, width=1.5, head=7)

    # permission branch
    c.arrow((cx + 165, 215), (686, 215), color=GREY, width=1.4, head=7,
            label="no", lpos=0.32, loff=(0, -12), lsize=10.5, lcolor=GREY)
    flow_node(c, 810, 215, 240, 50, "Request permission", fill=WHITE, stroke=GREY)
    c.arrow((810, 240), (810, 285), color=GREY, width=1.4, head=7, dash=True,
            label="denied", lpos=0.5, loff=(34, 0), lsize=9.5, lcolor=GREY)
    flow_node(c, 810, 312, 240, 54, "City search → coordinates", sub="planned · UC-05", fill=WHITE, stroke=GREY_LIGHT)

    flow_node(c, cx, 345, 400, 54, "Acquire coordinates (FusedLocationProviderClient)", fill=WHITE, stroke=BLUE)
    c.arrow((cx, 372), (cx, 402), color=INK, width=1.5, head=7)
    diamond(c, cx, 458, 330, 112, "Coordinates available?")
    c.text(cx + 44, 535, "yes", size=10.5, color=TEAL, anchor="lm")
    c.arrow((cx, 514), (cx, 560), color=INK, width=1.5, head=7)

    c.arrow((cx + 165, 458), (686, 458), color=GREY, width=1.4, head=7,
            label="no", lpos=0.32, loff=(0, -12), lsize=10.5, lcolor=GREY)
    flow_node(c, 810, 458, 240, 54, "IP geolocation → coordinates", sub="planned", fill=WHITE, stroke=GREY_LIGHT)

    flow_node(c, cx, 590, 400, 54, "Build request: current fields + timezone = auto")
    # fallback bus
    poly_arrow(c, [(930, 312), (966, 312), (966, 590), (664, 590)], color=GREY, width=1.3, head=7)
    c.line([(930, 458), (966, 458)], color=GREY, width=1.3)
    c.label(822, 578, "fallback coordinates (planned)", size=9.5, color=GREY, bg=WHITE)

    c.arrow((cx, 617), (cx, 652), color=INK, width=1.5, head=7)
    flow_node(c, cx, 680, 400, 54, "HTTPS GET api.open-meteo.com/v1/forecast", fill=TEAL_SOFT, stroke=TEAL)
    c.arrow((cx, 707), (cx, 742), color=INK, width=1.5, head=7)
    diamond(c, cx, 796, 330, 106, "HTTP 200 and non-null body?")
    c.text(cx + 44, 872, "yes", size=10.5, color=TEAL, anchor="lm")
    c.arrow((cx, 849), (cx, 886), color=INK, width=1.5, head=7)

    c.arrow((cx + 165, 796), (686, 796), color=GREY, width=1.4, head=7,
            label="no", lpos=0.32, loff=(0, -12), lsize=10.5, lcolor=GREY)
    flow_node(c, 810, 796, 240, 54, "Log error · retry (planned)", sub="UC-09", fill=RED_SOFT, stroke=RED)

    flow_node(c, cx, 914, 400, 54, "Parse JSON (Gson) → WeatherResponse")
    c.arrow((cx, 941), (cx, 976), color=INK, width=1.5, head=7)
    flow_node(c, cx, 1004, 400, 54, "Bind views + select icon by weather_code", fill=BLUE_SOFT, stroke=BLUE)
    c.arrow((cx, 1031), (cx, 1058), color=INK, width=1.5, head=7)
    c.ellipse(cx - 13, 1062, 26, 26, fill=WHITE, outline=INK, width=1.4)
    c.ellipse(cx - 7, 1068, 14, 14, fill=INK)
    c.text(cx + 30, 1075, "End", size=10.5, color=GREY, anchor="lm")

    c.ellipse(800, 950, 22, 22, fill=RED_SOFT, outline=RED, width=1.4)
    c.ellipse(809, 959, 4, 4, fill=RED)
    c.text(830, 952, "End (error)", size=10, color=RED, anchor="lm")
    c.arrow((810, 823), (810, 948), color=RED, width=1.3, head=7, dash=True)

    return c.save(os.path.join(OUT, "fig_flow.png"))


# ----------------------------------------------------------------- figure 6
def fig_wire():
    c = Canvas(1000, 800)
    c.rect(60, 26, 380, 740, r=34, fill="#E9EEF7", outline="#C3CEDF", width=1.5, shadow=True)
    c.rect(72, 40, 356, 712, r=28, fill="#F4F7FC")
    c.text(96, 58, "9:41", size=9.5, weight="bold", color="#3C4A61", anchor="lm")
    for i in range(3):
        c.rect(372 + i * 9, 55, 6, 7 - i, r=1.5, fill="#3C4A61")

    # location header
    c.text(100, 92, "YOUR LOCATION", size=7.6, weight="bold", color="#8493A8", anchor="lm")
    c.text(100, 112, "Faisalabad, Pakistan", size=14.5, weight="bold", color="#172B4D", anchor="lm")
    sun(c, 384, 100, r=9, color="#F5B942")
    badge(c, 424, 112, 1)

    # current conditions card
    c.gradient_rect(96, 140, 308, 210, 16, (42, 91, 215), (31, 59, 120), outline="#4A78DE", width=0.8)
    c.text(112, 162, "TODAY'S WEATHER", size=7.6, weight="bold", color="#D7E7FF", anchor="lm")
    c.text(112, 205, "28°", size=42, weight="bold", color=WHITE, anchor="lm")
    c.text(112, 238, "Mostly Sunny", size=12.5, weight="bold", color=WHITE, anchor="lm")
    c.text(112, 257, "Feels like 30°", size=9.5, color="#D7E7FF", anchor="lm")

    sun(c, 330, 190, r=11, color="#FFD37A")
    cloud(c, 352, 214, s=0.85, color=WHITE, shade="#D7E7FF")
    c.line([(112, 282), (388, 282)], color=(255, 255, 255), width=1.0, dash=(3, 4))
    c.text(112, 300, "H: 30°    L: 20°", size=9, color=WHITE, anchor="lm")
    c.text(388, 300, "Live weather", size=8.5, color="#D7E7FF", anchor="rm")
    badge(c, 424, 152, 2)

    # details
    c.text(96, 378, "Weather Details", size=13, weight="bold", color="#172B4D", anchor="lm")
    for i, (label, value) in enumerate((("HUMIDITY", "65%"), ("WIND SPEED", "12 km/h"))):
        x = 96 + i * 158
        c.rect(x, 392, 148, 76, r=12, fill=WHITE, outline="#E2E9F4", width=1)
        c.text(x + 14, 414, label, size=7, weight="bold", color="#8493A8", anchor="lm")
        c.text(x + 14, 440, value, size=14.5, weight="bold", color="#172B4D", anchor="lm")
    badge(c, 424, 400, 3)

    # hourly
    c.text(96, 496, "Hourly Forecast", size=13, weight="bold", color="#172B4D", anchor="lm")
    for i, (t, temp) in enumerate((("NOW", "28°"), ("2 PM", "29°"), ("3 PM", "27°"), ("4 PM", "25°"))):
        x = 96 + i * 78
        fill, tc = ("#2A5BD7", WHITE) if i == 0 else (WHITE, "#172B4D")
        c.rect(x, 512, 70, 90, r=12, fill=fill, outline="#E2E9F4", width=1)
        c.text(x + 35, 528, t, size=7.6, weight="bold" if i == 0 else "regular", color=tc if i == 0 else "#718096")
        if i == 0:
            sun(c, x + 35, 558, r=8, color="#FFD37A")
        elif i == 1:
            cloud(c, x + 33, 558, s=0.46)
        elif i == 2:
            cloud(c, x + 33, 558, s=0.46, color="#DCE5F3", shade="#C3D0E4")
        else:
            cloud(c, x + 33, 556, s=0.46, color="#DCE5F3", shade="#C3D0E4")
            for k in range(3):
                c.line([(x + 23 + k * 9, 566), (x + 20 + k * 9, 577)], color="#7FA8E8", width=1.5)
        c.text(x + 35, 589, temp, size=10.5, weight="bold", color=tc)
    badge(c, 424, 520, 4)

    # daily
    c.text(96, 632, "7-Day Forecast", size=13, weight="bold", color="#172B4D", anchor="lm")
    c.rect(96, 648, 308, 92, r=12, fill=WHITE, outline="#E2E9F4", width=1)
    rows = (("Today", "30° / 20°"), ("Tomorrow", "29° / 21°"), ("Wednesday", "26° / 19°"))
    for i, (day, rng) in enumerate(rows):
        y = 664 + i * 26
        c.text(110, y + 7, day, size=9, weight="bold" if i == 0 else "regular", color="#172B4D", anchor="lm")
        cloud(c, 300, y + 1, s=0.32)
        c.text(392, y + 7, rng, size=9, color="#172B4D", anchor="rm")
        if i < 2:
            c.d.line([(110 * 2, (y + 19) * 2), (392 * 2, (y + 19) * 2)], fill=(226, 233, 244, 255), width=2)
    badge(c, 424, 656, 5)

    # legend
    c.rect(470, 60, 500, 690, r=14, fill=PANEL, outline=LINE, width=1.2)
    c.text(494, 88, "Layout regions  (activity_main.xml)", size=13.5, weight="bold", color=NAVY, anchor="lm")
    items = [
        ("1", "Location header", "tvLocation — filled from the detected city or a search result"),
        ("2", "Current conditions card", "tvTemperature, tvCondition, tvFeelsLike, ivCurrentWeather, tvHighLow"),
        ("3", "Metric tiles", "tvHumidity, tvWindSpeed — two equal-width cards"),
        ("4", "Hourly strip", "HorizontalScrollView + bg_hour_item / bg_hour_selected"),
        ("5", "7-day list", "dailyForecastContainer — one row per day with a divider"),
    ]
    y = 124
    for num, title, sub in items:
        badge(c, 512, y + 4, num)
        c.text(534, y, title, size=12.3, weight="bold", color=INK, anchor="lm")
        c.para(534, y + 20, 400, sub, size=10, color=GREY, spacing=1.26)
        y += 84
    c.d.line([(494 * 2, 556 * 2), (946 * 2, 556 * 2)], fill=(198, 210, 228, 255), width=2)
    c.text(494, 580, "Design notes", size=11, weight="bold", color=NAVY, anchor="lm")
    c.para(494, 598, 420,
           "ScrollView root · Material3 DayNight theme · 20 dp page padding · card radii 14–18 dp. "
           "Static preview values are shown where the live binding is not yet implemented.",
           size=9.8, color=GREY, spacing=1.3)
    return c.save(os.path.join(OUT, "fig_wire.png"))


# ----------------------------------------------------------------- figure 7
def fig_class():
    c = Canvas(1000, 700)

    def cls(x, y, w, title, stereo, attrs, methods):
        head = 56 if stereo else 40
        body = y + head
        h = head + 16 + 20 * len(attrs) + 12 + 20 * len(methods) + 8
        c.rect(x, y, w, h, r=6, fill=WHITE, outline=NAVY, width=1.3, shadow=True)
        c.rect(x, y, w, head, r=6, fill=NAVY, outline=NAVY, width=1)
        c.rect(x, y + head - 7, w, 7, r=0, fill=NAVY)
        if stereo:
            c.text(x + w / 2, y + 15, stereo, size=9.5, color="#D7E7FF")
            c.text(x + w / 2, y + 37, title, size=12.5, weight="bold", color=WHITE)
        else:
            c.text(x + w / 2, y + 21, title, size=12.5, weight="bold", color=WHITE)
        yy = body + 10
        for a in attrs:
            c.text(x + 12, yy, a, size=9.4, color=INK, anchor="lm")
            yy += 20
        c.line([(x, yy - 4), (x + w, yy - 4)], color=LINE, width=1)
        yy += 8
        for m in methods:
            c.text(x + 12, yy, m, size=9.4, color="#23406B", anchor="lm")
            yy += 20
        return {"x": x, "y": y, "w": w, "h": h}

    a = cls(40, 70, 230, "RetrofitClient", None,
            ["- BASE_URL : String", "- retrofit : Retrofit"],
            ["+ getApi() : WeatherApi"])
    b = cls(330, 50, 250, "WeatherApi", "«interface»",
            ["+ BASE_URL : String"],
            ["+ getWeather(lat, lon,", "   current, hourly, daily,", "   timezone, days)", "   : Call<WeatherResponse>"])
    d = cls(690, 50, 260, "WeatherResponse", None,
            ["+ latitude : double", "+ longitude : double", "+ timezone : String", "+ current : CurrentWeather"],
            ["+ getters / toString()"])
    e = cls(690, 360, 260, "CurrentWeather", "«nested class»",
            ["+ time : String", "+ temperature : double", "+ humidity : int", "+ feelsLike : double",
             "+ isDay : int", "+ precipitation : double", "+ weatherCode : int", "+ windSpeed : double"],
            ["+ getters"])
    f = cls(40, 380, 230, "MainActivity", None,
            ["- api : WeatherApi", "- views : TextView[]"],
            ["# onCreate(Bundle)", "+ loadWeather()", "+ bindViews()"])

    c.arrow((a["x"] + a["w"], a["y"] + 44), (b["x"] - 8, a["y"] + 44), color=GREY, width=1.5, head=12, dash=True,
            label="«create»", lpos=0.5, loff=(0, -12), lsize=9.5, lcolor=GREY)
    c.arrow((b["x"] + b["w"], b["y"] + 40), (d["x"] - 8, b["y"] + 40), color=GREY, width=1.5, head=12, dash=True,
            label="«returns»", lpos=0.5, loff=(0, -12), lsize=9.5, lcolor=GREY)

    mid = d["x"] + d["w"] * 0.55
    top = d["y"] + d["h"]
    c.line([(mid, top + 22), (mid, e["y"])], color=NAVY, width=1.5)
    c.polygon([(mid, top), (mid - 9, top + 11), (mid, top + 22), (mid + 9, top + 11)], fill=WHITE, outline=NAVY)
    c.text(mid + 14, top + 30, "1", size=10, color=NAVY, anchor="lm")
    c.text(mid + 14, e["y"] - 12, "1", size=10, color=NAVY, anchor="lm")

    c.arrow((f["x"] + 120, f["y"] - 6), (f["x"] + 120, a["y"] + a["h"] + 8), color=GREY, width=1.4, head=12, dash=True,
            label="«uses»", lpos=0.5, loff=(38, 0), lsize=9.5, lcolor=GREY)
    c.arrow((f["x"] + f["w"], f["y"] + 90), (e["x"] - 8, f["y"] + 90), color=GREY, width=1.4, head=12, dash=True,
            label="«reads»", lpos=0.35, loff=(0, -12), lsize=9.5, lcolor=GREY)

    c.text(40, 680, "Fields are public in the current implementation; the interface shown here is the intended encapsulation for release 1.0.",
           size=10, color=GREY_LIGHT, anchor="lm")
    return c.save(os.path.join(OUT, "fig_class.png"))


# ----------------------------------------------------------------- figure 8
def fig_gantt():
    c = Canvas(1000, 470)
    left, right = 250, 950
    weeks = 9
    col_w = (right - left) / weeks
    rows = [
        ("Requirements & SRS", 0, 2, "done"),
        ("UI design & layouts", 1, 2, "done"),
        ("Retrofit client & models", 2, 2, "done"),
        ("Location integration", 3, 2, "active"),
        ("Hourly & daily forecast", 4, 2, "plan"),
        ("City search", 5, 2, "plan"),
        ("Testing & QA", 6, 2, "plan"),
        ("Documentation & submission", 7, 2, "plan"),
    ]
    colors = {"done": TEAL, "active": AMBER, "plan": "#A9C4F2"}
    labels = {"done": "complete", "active": "in progress", "plan": "planned"}

    for w in range(weeks + 1):
        x = left + w * col_w
        c.line([(x, 52), (x, 392)], color="#E3EAF5", width=1)
    for w in range(weeks):
        c.text(left + (w + 0.5) * col_w, 34, f"W{w + 1}", size=10.5, weight="semibold", color=GREY)
    c.text(24, 34, "Phase", size=10, weight="bold", color=GREY_LIGHT, anchor="lm")

    for i, (name, start, dur, state) in enumerate(rows):
        y = 68 + i * 41
        c.text(28, y + 9, name, size=11.5, weight="semibold" if state != "plan" else "regular", color=INK, anchor="lm")
        x = left + start * col_w + 3
        c.rect(x, y, dur * col_w - 6, 18, r=9, fill=colors[state],
               outline=None if state != "plan" else "#7FA3DC", width=1.0)
        if state == "done":
            mx, my = x + dur * col_w / 2 - 3, y + 9
            c.line([(mx - 4, my), (mx - 1, my + 3), (mx + 5, my - 4)], color=WHITE, width=1.6)

    for i, mx in enumerate((2, 4, 6, 9)):
        px = left + mx * col_w
        c.polygon([(px, 396), (px - 7, 406), (px, 416), (px + 7, 406)], fill=NAVY)
        c.text(px, 432, f"M{i + 1}", size=9, color=NAVY)

    lx = 588
    for i, key in enumerate(("done", "active", "plan")):
        x = lx + i * 118
        c.rect(x, 10, 14, 10, r=4, fill=colors[key], outline=None if key != "plan" else "#7FA3DC", width=0.9)
        c.text(x + 20, 15, labels[key], size=10, color=GREY, anchor="lm")
    return c.save(os.path.join(OUT, "fig_gantt.png"))


if __name__ == "__main__":
    for fn in (fig_context, fig_arch, fig_usecase, fig_sequence, fig_flow, fig_wire, fig_class, fig_gantt):
        print(fn())
