package com.example.gymtracker.ui.common

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun NumericTextField(
    value: Int?,
    onValueChange: (value: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    GenericNumericTextField(
        value = value,
        onValueChange = { onValueChange(it.toIntOrNull() ?: 0) },
        modifier = modifier
    )
}

@Composable
fun NumericTextField(
    value: Double?,
    onValueChange: (value: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    GenericNumericTextField(
        value = value,
        onValueChange = { onValueChange(it.toDoubleOrNull() ?: 0.0) },
        modifier = modifier
    )
}

@Composable
private fun GenericNumericTextField(
    value: Number?,
    onValueChange: (value: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var valueString by remember { mutableStateOf(value.toString()) }
    OutlinedTextField(
        value = valueString,
        onValueChange = {
            valueString = it
            onValueChange(it)
        },
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Number
        ),
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        textStyle = TextStyle(textAlign = TextAlign.End),
        modifier = modifier.width(60.dp)
    )
}