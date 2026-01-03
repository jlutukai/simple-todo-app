package com.lutukai.simpletodoapp.ui.components.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A reusable header component for modal bottom sheets.
 * Displays a title in the center with optional leading and trailing actions.
 *
 * @param title The title text to display in the center
 * @param modifier Modifier to be applied to the component
 * @param leadingAction Optional composable for the leading action (e.g., Cancel button)
 * @param trailingAction Optional composable for the trailing action (e.g., Done button)
 */
@Composable
fun ModalHeader(
    title: String,
    modifier: Modifier = Modifier,
    leadingAction: @Composable (() -> Unit)? = null,
    trailingAction: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Leading action or spacer for balance
        if (leadingAction != null) {
            leadingAction()
        } else {
            Spacer(modifier = Modifier.width(48.dp))
        }

        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Trailing action or spacer for balance
        if (trailingAction != null) {
            trailingAction()
        } else {
            Spacer(modifier = Modifier.width(48.dp))
        }
    }
}

/**
 * A text button commonly used in modal headers for actions like Cancel or Done.
 *
 * @param text The button text
 * @param onClick Callback invoked when the button is clicked
 * @param modifier Modifier to be applied to the button
 */
@Composable
fun ModalHeaderTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
