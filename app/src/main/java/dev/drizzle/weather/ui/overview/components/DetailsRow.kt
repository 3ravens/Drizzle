package dev.drizzle.weather.ui.overview.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.drizzle.weather.domain.model.WeatherForecast
import dev.drizzle.weather.util.TimeFormat
import kotlin.math.roundToInt

@Composable
fun DetailsRow(
    forecast: WeatherForecast,
    modifier: Modifier = Modifier,
) {
    val current = forecast.current
    val sun = listOfNotNull(
        forecast.sunrise?.let { TimeFormat.time(it) },
        forecast.sunset?.let { TimeFormat.time(it) },
    ).joinToString(" · ").ifBlank { "—" }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailItem(
                icon = Icons.Filled.Air,
                label = "Wind",
                value = "${current.windSpeed.roundToInt()} km/h",
                modifier = Modifier.weight(1f),
            )
            DetailItem(
                icon = Icons.Filled.WaterDrop,
                label = "Humidity",
                value = "${current.humidity}%",
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailItem(
                icon = Icons.Filled.Umbrella,
                label = "Precipitation",
                value = "${formatNumber(current.precipitation)} mm",
                modifier = Modifier.weight(1f),
            )
            DetailItem(
                icon = Icons.Filled.WbTwilight,
                label = "Sun",
                value = sun,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DetailItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(2.dp))
            Text(text = value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun formatNumber(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else "%.1f".format(value)
