package dev.drizzle.weather.ui.overview.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.drizzle.weather.domain.model.WeatherForecast
import dev.drizzle.weather.util.WeatherCode
import kotlin.math.roundToInt

@Composable
fun CurrentCard(
    forecast: WeatherForecast,
    modifier: Modifier = Modifier,
) {
    val current = forecast.current
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = WeatherCode.icon(current.weatherCode, current.isDay),
            contentDescription = WeatherCode.description(current.weatherCode),
            modifier = Modifier.size(84.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "${current.temperature.roundToInt()}°",
            style = MaterialTheme.typography.displayLarge,
        )
        Text(
            text = WeatherCode.description(current.weatherCode),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            forecast.dailyHigh?.let {
                Text("H ${it.roundToInt()}°", style = MaterialTheme.typography.labelMedium)
            }
            forecast.dailyLow?.let {
                Text("L ${it.roundToInt()}°", style = MaterialTheme.typography.labelMedium)
            }
            Text(
                "Feels like ${current.apparentTemperature.roundToInt()}°",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
