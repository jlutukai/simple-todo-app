package com.lutukai.simpletodoapp.ui.components.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * A header component for modal bottom sheets with icon buttons.
 * Displays a leading icon button on the left and a trailing icon button on the right.
 *
 * @param modifier Modifier to be applied to the component
 * @param leadingIcon Optional leading icon configuration
 * @param trailingIcon Optional trailing icon configuration
 */
@Composable
fun ModalIconHeader(
    modifier: Modifier = Modifier,
    leadingIcon: IconButtonConfig? = null,
    trailingIcon: IconButtonConfig? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            IconButton(
                onClick = leadingIcon.onClick,
                modifier = leadingIcon.modifier
            ) {
                Icon(
                    imageVector = leadingIcon.icon,
                    contentDescription = leadingIcon.contentDescription,
                    tint = leadingIcon.tint
                )
            }
        }

        if (trailingIcon != null) {
            IconButton(
                onClick = trailingIcon.onClick,
                modifier = trailingIcon.modifier
            ) {
                Icon(
                    imageVector = trailingIcon.icon,
                    contentDescription = trailingIcon.contentDescription,
                    tint = trailingIcon.tint
                )
            }
        }
    }
}

/**
 * Configuration for an icon button in the modal header.
 *
 * @param icon The icon to display
 * @param onClick Callback invoked when the button is clicked
 * @param contentDescription Content description for accessibility
 * @param tint The tint color for the icon
 * @param modifier Modifier to be applied to the button
 */
data class IconButtonConfig(
    val icon: ImageVector,
    val onClick: () -> Unit,
    val contentDescription: String?,
    val tint: Color = Color.Unspecified,
    val modifier: Modifier = Modifier
)

/**
 * Helper function to create an IconButtonConfig with primary color tint.
 */
@Composable
fun primaryIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String?,
    modifier: Modifier = Modifier
): IconButtonConfig = IconButtonConfig(
    icon = icon,
    onClick = onClick,
    contentDescription = contentDescription,
    tint = MaterialTheme.colorScheme.primary,
    modifier = modifier
)

/**
 * Helper function to create an IconButtonConfig with surface variant color tint.
 */
@Composable
fun secondaryIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String?,
    modifier: Modifier = Modifier
): IconButtonConfig = IconButtonConfig(
    icon = icon,
    onClick = onClick,
    contentDescription = contentDescription,
    tint = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier = modifier
)
