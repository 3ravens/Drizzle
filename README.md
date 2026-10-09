# Drizzle

An ad-free, fast, single-overview Android weather app. No backend — every
request goes straight from your phone to [Open-Meteo](https://open-meteo.com),
using the selected location. No ads, no tracking, no analytics.

## Features

- **Current weather** — big temperature, condition, feels-like, high/low.
- **Rain graph** — headline ("Rain starting at 15:30" / "No rain expected")
  with a 15-minute bar chart for the next 6 hours.
- **Hourly strip** — the next 24 hours: time, icon, temperature, rain chance.
- **Details** — wind, humidity, precipitation, sunrise/sunset.
- **Saved locations** — a pinned "Current location" plus manual cities
  (search backed by the Open-Meteo geocoding API).
- **Offline** — the last forecast is cached and shown with a stale indicator.
- **Settings** — units (metric), active location, about/attribution.

## Tech stack

Kotlin · Jetpack Coroutines/Flow · Jetpack Compose (Material 3) ·
MVVM + Repository · Retrofit + kotlinx.serialization · Room · DataStore ·
FusedLocationProvider · manual DI (no Hilt).

- `minSdk 26`, `targetSdk 35`, `compileSdk 35`.
- Data source: Open-Meteo (free, **no API key**).

## Building

Requirements:

- **JDK 17** (the Android Gradle Plugin 8.x toolchain).
- **Android Studio** (Ladybug or newer) or the Android SDK with
  build-tools for API 35.

Open the project folder in Android Studio and let it sync. It will download
Gradle 8.10.2 (per `gradle/wrapper/gradle-wrapper.properties`) and the required
SDK components.

> The `gradle/wrapper/gradle-wrapper.jar` is not committed. Android Studio
> reads `gradle-wrapper.properties` directly, so syncing works without it. For
> command-line builds, run `gradle wrapper` once (with Gradle installed) to
> generate the wrapper jar and scripts.

Run the unit tests:

```
./gradlew testDebugUnitTest
```

## Project layout

```
app/src/main/java/dev/drizzle/weather/
  DrizzleApp.kt              Application + entry point to the DI graph
  MainActivity.kt
  di/AppContainer.kt         Manual dependency graph
  data/
    remote/                  Retrofit APIs (forecast + geocoding) and DTOs
    local/                   Room database, DAO and entities
    mapper/WeatherMapper.kt  Open-Meteo payload -> domain models
    repository/              Weather + Location repositories (single source of truth)
    settings/SettingsDataStore.kt
  domain/model/              Pure Kotlin models
  location/LocationProvider.kt
  ui/
    nav/                     Compose navigation
    overview/                Overview screen, state and view model
    overview/components/     CurrentCard, RainGraph, HourlyStrip, DetailsRow
    locations/               Saved-locations bottom sheet + view model
    settings/                Settings screen
    theme/                   Material 3 theme
  util/                      Weather-code mapping, time formatting
```

## How the pieces fit

```
MainActivity -> DrizzleNavHost
    OverviewScreen  <- OverviewViewModel  -\
    SettingsScreen  <- SettingsViewModel   -> WeatherRepository / LocationRepository
                                              |-- OpenMeteoApi    (retrofit)
                                              |-- GeocodingApi    (retrofit)
                                              |-- Room            (cache + saved locations)
                                              \-- LocationProvider (FusedLocation)
```

- The repositories are the single source of truth. The overview renders the
  cached forecast immediately, then refreshes from the network.
- Weather codes are mapped to text/icons locally (`util/WeatherCode.kt`).
- Timestamps come back from Open-Meteo as location-local ISO strings
  (`timezone=auto`), so the rain window and hourly list are computed against
  the location-local `current.time` — no timezone conversions.

## Attribution

Weather data by [Open-Meteo.com](https://open-meteo.com), which is free for
non-commercial use and requires credit. The credit is shown in Settings → About.
