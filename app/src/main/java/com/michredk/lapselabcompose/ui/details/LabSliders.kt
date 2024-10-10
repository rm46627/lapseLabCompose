package com.michredk.lapselabcompose.ui.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.michredk.lapselabcompose.R
import kotlin.math.roundToInt

@Composable
fun PeaceSlider(currentValue: Int, peaceOnValueChange: (Int) -> Unit) {
    val strValues = listOf(stringResource(R.string.super_slow),
        stringResource(R.string.slow), stringResource(R.string.moderate),
        stringResource(R.string.fast), stringResource(R.string.super_fast)
    )
    val intValues = listOf(20, 10, 5, 2, 1)

    val position = intValues.indexOf(currentValue).takeIf { it != -1 }?.toFloat() ?: 1f
    var sliderPosition by remember { mutableFloatStateOf(position) }

    val currentIndex = sliderPosition.roundToInt().coerceIn(0, strValues.size - 1)
    val selectedValue = strValues[currentIndex]

    Column(
        modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = stringResource(R.string.video_peace), style = MaterialTheme.typography.titleMedium)
        Slider(
            value = sliderPosition,
            onValueChange = { newPosition ->
                sliderPosition = newPosition
                peaceOnValueChange(intValues[newPosition.roundToInt().coerceIn(0, intValues.size - 1)])
            },
            valueRange = 0f..(strValues.size - 1).toFloat(),
            steps = strValues.size - 2
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = selectedValue, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
fun BitrateSlider(currentValue: Int, bitrateOnValueChange: (Int) -> Unit) {
    val strValues = listOf(stringResource(R.string.low), stringResource(R.string.moderate),
        stringResource(
            R.string.high
        )
    )
    val intValues = listOf(1000000, 1500000, 2000000)

    val position = intValues.indexOf(currentValue).takeIf { it != -1 }?.toFloat() ?: 1f
    var sliderPosition by remember { mutableFloatStateOf(position) }

    val currentIndex = sliderPosition.roundToInt().coerceIn(0, strValues.size - 1)
    val selectedValue = strValues[currentIndex]

    Column(
        modifier = Modifier.padding(horizontal = 32.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = stringResource(R.string.quality), style = MaterialTheme.typography.titleMedium)
        Slider(
            value = sliderPosition,
            onValueChange = { newPosition ->
                sliderPosition = newPosition
                bitrateOnValueChange(intValues[newPosition.roundToInt().coerceIn(0, intValues.size - 1)])
            },
            valueRange = 0f..(strValues.size - 1).toFloat(),
            steps = strValues.size - 2
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = selectedValue, style = MaterialTheme.typography.titleSmall)
    }
}
//@Composable
//fun BitrateSlider(currentValue: Int, bitrateOnValueChange: (Int) -> Unit) {
//    val values = listOf(1000000, 1250000, 1500000, 1750000, 2000000)
//
//    // Determine the slider's position based on the current value
//    var sliderPosition by remember {
//        mutableFloatStateOf(values.indexOf(currentValue).takeIf { it != -1 }?.toFloat() ?: 1f)
//    }
//
//    // Calculate the current index and selected value based on slider position
//    val currentIndex = sliderPosition.roundToInt().coerceIn(0, values.size - 1)
//    val selectedValue = values[currentIndex]
//
//    Column(
//        modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp),
//        horizontalAlignment = Alignment.Start
//    ) {
//        Text(text = "Bitrate", style = MaterialTheme.typography.titleMedium)
//        Spacer(modifier = Modifier.height(4.dp))
//        Slider(
//            value = sliderPosition,
//            onValueChange = { newPosition ->
//                sliderPosition = newPosition
//                bitrateOnValueChange(values[newPosition.roundToInt().coerceIn(0, values.size - 1)])
//            },
//            valueRange = 0f..(values.size - 1).toFloat(),
//            steps = values.size - 2
//        )
//        Spacer(modifier = Modifier.height(2.dp))
//        Text(text = "${selectedValue / 1000} kb/sec", style = MaterialTheme.typography.titleSmall)
//    }
//}