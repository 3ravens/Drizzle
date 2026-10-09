package dev.drizzle.weather.ui.overview.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import dev.drizzle.weather.domain.model.RainMode
import dev.drizzle.weather.domain.model.RainWindow
import dev.drizzle.weather.util.TimeFormat
import java.util.Locale
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

private val CHART_HEIGHT = 96.dp
private val AXIS_GAP = 6.dp

@Composable
fun RainGraph(
    rain: RainWindow,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = headline(rain),
                style = MaterialTheme.typography.titleMedium,
            )
            caption(rain)?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (rain.points.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))

                val axisMax = axisMaxFor(rain)
                val unit = unitFor(rain)
                val barColor = MaterialTheme.colorScheme.primary
                val highlight = MaterialTheme.colorScheme.tertiary
                val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)

                Row(modifier = Modifier.fillMaxWidth()) {
                    // Y axis: max at the top, half in the middle, zero at the bottom.
                    Column(
                        modifier = Modifier.height(CHART_HEIGHT),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End,
                    ) {
                        AxisLabel(formatLevel(axisMax, unit))
                        AxisLabel(formatLevel(axisMax / 2f, unit))
                        AxisLabel("0")
                    }

                    Spacer(Modifier.width(AXIS_GAP))

                    Column(modifier = Modifier.weight(1f)) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(CHART_HEIGHT),
                        ) {
                            val count = rain.points.size
                            val slot = size.width / count
                            val barWidth = slot * 0.6f
                            val highlightIndex = rain.points.indexOfFirst { it.value > 0f }

                            listOf(0f, 0.5f, 1f).forEach { fraction ->
                                val y = size.height * fraction
                                drawLine(
                                    color = gridColor,
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = 1f,
                                )
                            }

                            rain.points.forEachIndexed { index, point ->
                                val fraction = (point.value / axisMax).coerceIn(0f, 1f)
                                val barHeight =
                                    if (fraction <= 0f) 2.dp.toPx() else fraction * size.height
                                val left = index * slot + (slot - barWidth) / 2f
                                drawRoundRect(
                                    color = if (index == highlightIndex) highlight else barColor.copy(alpha = 0.8f),
                                    topLeft = Offset(left, size.height - barHeight),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = TimeFormat.time(rain.points.first().time),
                                style = MaterialTheme.typography.labelMedium,
                            )
                            Text(
                                text = TimeFormat.time(rain.points.last().time),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AxisLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private fun headline(rain: RainWindow): String = when {
    rain.points.isEmpty() -> "Rain outlook unavailable"
    rain.startsAt != null -> "Rain starting at ${TimeFormat.time(rain.startsAt)}"
    rain.mode == RainMode.MINUTELY -> "No rain expected in the next 6 hours"
    else -> "No rain expected in the next ${rain.points.size} hours"
}

private fun caption(rain: RainWindow): String? = when (rain.mode) {
    RainMode.MINUTELY -> "Next 6 hours · 15-minute forecast"
    RainMode.HOURLY -> "Next 6 hours · hourly forecast"
    RainMode.NONE -> null
}

private fun unitFor(rain: RainWindow): String = when (rain.mode) {
    RainMode.MINUTELY, RainMode.HOURLY -> "mm"
    RainMode.NONE -> ""
}

/** Fixed upper bound so bar heights reflect the real rain level, not the data's own peak. */
private fun axisMaxFor(rain: RainWindow): Float {
    val dataMax = rain.points.maxOfOrNull { it.value } ?: 0f
    return niceCeil(dataMax * 1.15f) // small headroom so the peak is not glued to the top
}

/** Rounds up to a readable 1 / 1.5 / 2 / 3 / 5 / 7.5 / 10 step. */
private fun niceCeil(value: Float): Float {
    if (value <= 0f) return 0.5f
    val exponent = floor(log10(value.toDouble()))
    val base = 10.0.pow(exponent)
    val normalized = value / base
    val nice = when {
        normalized <= 1.0 -> 1.0
        normalized <= 1.5 -> 1.5
        normalized <= 2.0 -> 2.0
        normalized <= 3.0 -> 3.0
        normalized <= 5.0 -> 5.0
        normalized <= 7.5 -> 7.5
        else -> 10.0
    }
    return (nice * base).toFloat()
}

private fun formatLevel(value: Float, unit: String): String {
    val number = if (value == value.toLong().toFloat()) value.toLong().toString()
    else String.format(Locale.getDefault(), "%.2f", value).trimEnd('0').trimEnd('.')
    return number + unit
}
