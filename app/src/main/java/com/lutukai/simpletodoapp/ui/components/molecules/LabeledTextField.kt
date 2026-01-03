package com.lutukai.simpletodoapp.ui.components.molecules

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp

/**
 * A reusable text field with a label above it.
 * Wraps OutlinedTextField with consistent app styling.
 *
 * @param label The label text displayed above the field
 * @param value The current text value
 * @param onValueChange Callback invoked when the text changes
 * @param modifier Modifier to be applied to the container
 * @param placeholder Optional placeholder text
 * @param singleLine Whether the field should be single line
 * @param minLines Minimum number of lines (for multiline fields)
 * @param keyboardOptions Keyboard configuration options
 * @param textFieldModifier Modifier to be applied specifically to the text field
 */
@Composable
fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    singleLine: Boolean = false,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.Sentences
    ),
    textFieldModifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = textFieldModifier.fillMaxWidth(),
            placeholder = placeholder?.let {
                {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            keyboardOptions = keyboardOptions,
            singleLine = singleLine,
            minLines = minLines,
            shape = RoundedCornerShape(12.dp)
        )
    }
}
