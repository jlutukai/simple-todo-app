package com.lutukai.simpletodoapp.ui.components.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * A generic segmented tabs component that can be used with any type of items.
 *
 * @param T The type of items in the tabs
 * @param items The list of items to display as tabs
 * @param selectedItem The currently selected item
 * @param onItemSelected Callback invoked when an item is selected
 * @param itemLabel Composable function to convert an item to its display label
 * @param modifier Modifier to be applied to the component
 * @param itemTestTag Optional function to generate test tags for each item
 */
@Composable
fun <T> SegmentedTabs(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    itemLabel: @Composable (T) -> String,
    modifier: Modifier = Modifier,
    itemTestTag: ((T) -> String)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp)
    ) {
        items.forEach { item ->
            val isSelected = item == selectedItem
            val label = itemLabel(item)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.surface
                        else Color.Transparent
                    )
                    .clickable { onItemSelected(item) }
                    .padding(vertical = 10.dp)
                    .let { mod ->
                        itemTestTag?.let { tagFn ->
                            mod.then(Modifier)
                        } ?: mod
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
