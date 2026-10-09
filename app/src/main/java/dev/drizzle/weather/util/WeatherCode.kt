package dev.drizzle.weather.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

/** Maps WMO weather codes (0-99) to human text and an icon. */
object WeatherCode {

    fun description(code: Int): String = when (code) {
        0 -> "Clear sky"
        1 -> "Mainly clear"
        2 -> "Partly cloudy"
        3 -> "Overcast"
        45, 48 -> "Fog"
        51, 53, 55 -> "Drizzle"
        56, 57 -> "Freezing drizzle"
        61 -> "Light rain"
        63 -> "Rain"
        65 -> "Heavy rain"
        66, 67 -> "Freezing rain"
        71 -> "Light snow"
        73 -> "Snow"
        75 -> "Heavy snow"
        77 -> "Snow grains"
        80 -> "Light showers"
        81 -> "Showers"
        82 -> "Violent showers"
        85, 86 -> "Snow showers"
        95 -> "Thunderstorm"
        96, 99 -> "Thunderstorm with hail"
        else -> "Unknown"
    }

    fun icon(code: Int, isDay: Boolean): ImageVector = when (code) {
        0 -> if (isDay) Icons.Filled.WbSunny else Icons.Filled.NightsStay
        1, 2 -> Icons.Filled.Cloud
        3 -> Icons.Filled.Cloud
        45, 48 -> Icons.Filled.Cloud
        51, 53, 55, 56, 57 -> Icons.Filled.Grain
        61, 63, 65, 80, 81, 82 -> Icons.Filled.WaterDrop
        66, 67, 71, 73, 75, 77, 85, 86 -> Icons.Filled.AcUnit
        95, 96, 99 -> Icons.Filled.Thunderstorm
        else -> Icons.Filled.Umbrella
    }
}
