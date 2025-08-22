package com.example.gymtracker.ui.common

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
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
        onValueChange = {
            onValueChange(it.toDoubleOrNull() ?: 0.0)
        },
        valueValidator = {
            val parts = it.split(",")
            parts[0].length <= valueMaxLength && (parts.getOrNull(1)?.length ?: 0) <= 2
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
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    fun Number.toCleanString(): String =
        BigDecimal.valueOf(this.toDouble()).stripTrailingZeros().toPlainString()

    var textFieldValue by remember {
        val initialText = value?.toCleanString() ?: "0"
        mutableStateOf(
            TextFieldValue(
                text = initialText,
                selection = TextRange(initialText.length)
            )
        )
    }

    var isFocused by remember { mutableStateOf(false) }

    LaunchedEffect(isFocused) {
        if (isFocused) {
            val currentText = textFieldValue.text
            val currentTextLength = currentText.length
            val newText = if (currentText == "0" || currentText == "0.0") "" else currentText
            textFieldValue = textFieldValue.copy(
                text = newText,
                selection = TextRange(currentTextLength)
            )
        } else {
            if (textFieldValue.text == "" || textFieldValue.text == ",") {
                textFieldValue = textFieldValue.copy(text = "0")
            }
        }
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = {
            val allowedCharacters =
                it.text.filter { input -> input.isDigit() || input == '.' || input == ',' }

            val newValue = allowedCharacters.replace(".", ",")
            val hasComma = newValue.count { char -> char == ','} > 1

            if (!hasComma && valueValidator(newValue)) {
                textFieldValue = it.copy(text = newValue)
                onValueChange(newValue.replace(",", "."))
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
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            }
    )
}