package com.lutukai.simpletodoapp.ui.components.molecules

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lutukai.simpletodoapp.R
import com.lutukai.simpletodoapp.domain.models.Todo
import com.lutukai.simpletodoapp.ui.preview.DevicePreviews
import com.lutukai.simpletodoapp.ui.theme.SimpleTodoAppTheme
import com.lutukai.simpletodoapp.ui.util.TestTags
import com.lutukai.simpletodoapp.ui.util.debouncedClickable

/**
 * A card component for displaying a single todo item with a checkbox,
 * title, and delete button.
 *
 * @param todo The todo item to display
 * @param onToggleComplete Callback invoked when the checkbox is toggled
 * @param onDelete Callback invoked when the delete button is clicked
 * @param onClick Callback invoked when the card is clicked
 * @param modifier Modifier to be applied to the component
 */
@Composable
fun TodoItemCard(
    todo: Todo,
    onToggleComplete: (Todo) -> Unit,
    onDelete: (Todo) -> Unit,
    onClick: (Todo) -> Unit,
    modifier: Modifier = Modifier
) {
    val todoId = todo.id ?: 0L
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
            .debouncedClickable(onClick = { onClick(todo) })
            .testTag(TestTags.todoItem(todoId)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 1.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = todo.isCompleted,
                onCheckedChange = { onToggleComplete(todo) },
                modifier = Modifier.testTag(TestTags.todoCheckbox(todoId))
            )

            Text(
                text = todo.title,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                color = if (todo.isCompleted) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else null,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge
            )

            IconButton(
                onClick = { onDelete(todo) },
                modifier = Modifier.testTag(TestTags.todoDeleteButton(todoId))
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.cd_delete_task),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@DevicePreviews
@Composable
private fun TodoItemCardPreview() {
    SimpleTodoAppTheme(dynamicColor = false) {
        TodoItemCard(
            todo = Todo(
                id = 1,
                title = "Buy groceries",
                description = "Milk, eggs, bread",
                isCompleted = false,
                completedAt = null,
                createdAt = System.currentTimeMillis()
            ),
            onToggleComplete = { },
            onDelete = { },
            onClick = { }
        )
    }
}

@DevicePreviews
@Composable
private fun TodoItemCardCompletedPreview() {
    SimpleTodoAppTheme(dynamicColor = false) {
        TodoItemCard(
            todo = Todo(
                id = 2,
                title = "Finish project report",
                description = "Complete the quarterly report",
                isCompleted = true,
                completedAt = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis() - 86_400_000
            ),
            onToggleComplete = { },
            onDelete = { },
            onClick = { }
        )
    }
}
