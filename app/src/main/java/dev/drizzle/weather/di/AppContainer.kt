package dev.drizzle.weather.di

import android.content.Context
import androidx.room.Room
import dev.drizzle.weather.BuildConfig
import dev.drizzle.weather.data.local.WeatherDatabase
import dev.drizzle.weather.data.remote.GeocodingApi
import dev.drizzle.weather.data.remote.OpenMeteoApi
import dev.drizzle.weather.data.repository.LocationRepository
import dev.drizzle.weather.data.repository.WeatherRepository
import dev.drizzle.weather.data.settings.SettingsDataStore
import dev.drizzle.weather.location.LocationProvider
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

/** Manual dependency graph. Simple enough to avoid Hilt. */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
    }

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BASIC
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            },
        )
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val database: WeatherDatabase = Room.databaseBuilder(
        appContext,
        WeatherDatabase::class.java,
        "drizzle.db",
    ).build()

    private val openMeteoApi: OpenMeteoApi = retrofit("https://api.open-meteo.com/")
        .create(OpenMeteoApi::class.java)

    private val geocodingApi: GeocodingApi = retrofit("https://geocoding-api.open-meteo.com/")
        .create(GeocodingApi::class.java)

    private fun retrofit(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttp)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    private val settings = SettingsDataStore(appContext)

    val locationProvider = LocationProvider(appContext)

    val weatherRepository = WeatherRepository(
        api = openMeteoApi,
        dao = database.weatherDao(),
        json = json,
    )

    val locationRepository = LocationRepository(
        geocodingApi = geocodingApi,
        dao = database.weatherDao(),
        settings = settings,
    )
}
