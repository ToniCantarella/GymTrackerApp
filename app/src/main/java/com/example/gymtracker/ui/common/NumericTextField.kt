package com.example.gymtracker.ui.common

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import java.math.BigDecimal

@Composable
fun NumericTextField(
    value: Int?,
    onValueChange: (value: Int) -> Unit,
    modifier: Modifier = Modifier,
    valueMaxLength: Int = 3
) {
    GenericNumericTextField(
        value = value,
        onValueChange = { onValueChange(it.toIntOrNull() ?: 0) },
        valueValidator = { it.length <= valueMaxLength && it.isDigitsOnly() },
        modifier = modifier.width(60.dp)
    )
}

@Composable
fun NumericTextField(
    value: Double?,
    onValueChange: (value: Double) -> Unit,
    modifier: Modifier = Modifier,
    valueMaxLength: Int = 3
) {
    GenericNumericTextField(
        value = value,
        onValueChange = { onValueChange(it.toDoubleOrNull() ?: 0.0) },
        valueValidator = {
            val parts = it.split(".")
            parts.size <= 2 && parts[0].length <= valueMaxLength && (parts.getOrNull(1)?.length
                ?: 0) <= 2
        },
        modifier = modifier.width(80.dp)
    )
}

@Composable
private fun GenericNumericTextField(
    value: Number?,
    onValueChange: (value: String) -> Unit,
    valueValidator: (value: String) -> Boolean,
    modifier: Modifier = Modifier
) {
    var valueString by remember { mutableStateOf(value.toString()) }
    OutlinedTextField(
        value = valueString,
        onValueChange = {
            val filtered = it.filter { input -> input.isDigit() || input == '.' }
            if (valueValidator(filtered)) {
                valueString = filtered
                onValueChange(filtered)
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                keyboardController?.hide()
                focusManager.clearFocus()
            }
        ),
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        maxLines = 1,
        textStyle = TextStyle(textAlign = TextAlign.End),
        modifier = modifier
    )
}