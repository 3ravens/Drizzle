package dev.drizzle.weather

import android.app.Application
import dev.drizzle.weather.di.AppContainer

class DrizzleApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
