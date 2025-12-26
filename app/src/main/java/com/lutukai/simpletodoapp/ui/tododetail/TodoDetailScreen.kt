package com.lutukai.simpletodoapp.ui.tododetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lutukai.simpletodoapp.ui.util.TestTags
import com.lutukai.simpletodoapp.R
import com.lutukai.simpletodoapp.domain.models.Todo
import com.lutukai.simpletodoapp.ui.mvi.ObserveAsEvents
import com.lutukai.simpletodoapp.ui.mvi.collectState
import com.lutukai.simpletodoapp.ui.mvi.rememberOnIntent
import com.lutukai.simpletodoapp.ui.preview.DevicePreviews
import com.lutukai.simpletodoapp.ui.theme.SimpleTodoAppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoDetailScreen(
    todoId: Long,
    onNavigateToEdit: (Long) -> Unit,
    onDismiss: () -> Unit,
    viewModel: TodoDetailMviViewModel = hiltViewModel()
) {
    val state = viewModel.collectState()
    val onIntent = viewModel.rememberOnIntent()
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(todoId) {
        viewModel.onIntent(TodoDetailIntent.LoadTodo(todoId))
    }

    ObserveAsEvents(flow = viewModel.effect) { effect ->
        when (effect) {
            is TodoDetailSideEffect.NavigateToEdit -> onNavigateToEdit(effect.todoId)
            is TodoDetailSideEffect.ShowError -> {
                snackbarHostState.showSnackbar(effect.message)
            }
            is TodoDetailSideEffect.Dismiss -> onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        TodoDetailContent(
            state = state,
            onIntent = onIntent,
            snackbarHostState = snackbarHostState
        )
    }
}

@Composable
internal fun TodoDetailContent(
    state: TodoDetailState,
    onIntent: (TodoDetailIntent) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val dateFormat = remember { SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { onIntent(TodoDetailIntent.EditClicked) },
                modifier = Modifier.testTag(TestTags.DETAIL_EDIT_BUTTON)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = stringResource(R.string.cd_edit_task),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(
                onClick = { onIntent(TodoDetailIntent.Dismiss) },
                modifier = Modifier.testTag(TestTags.DETAIL_CLOSE_BUTTON)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.cd_close),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .testTag(TestTags.DETAIL_LOADING),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            state.todo?.let { todo ->
                // Title
                Text(
                    text = todo.title,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag(TestTags.DETAIL_TITLE),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Completion Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.mark_complete),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = todo.isCompleted,
                        onCheckedChange = { onIntent(TodoDetailIntent.ToggleComplete(it)) },
                        modifier = Modifier.testTag(TestTags.DETAIL_COMPLETED_SWITCH)
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Notes Section
                Text(
                    text = stringResource(R.string.notes),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = todo.description.ifEmpty { "-" },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .testTag(TestTags.DETAIL_NOTES),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )

                HorizontalDivider(modifier = Modifier.padding(16.dp))

                // Metadata
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.created_on),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = todo.createdAt?.let { dateFormat.format(Date(it)) } ?: "-",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (todo.isCompleted && todo.completedAt != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.completed_on),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = dateFormat.format(Date(todo.completedAt)),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}

@DevicePreviews
@Composable
private fun TodoDetailContentPreview() {
    SimpleTodoAppTheme(dynamicColor = false) {
        TodoDetailContent(
            state = TodoDetailState(
                todo = Todo(
                    id = 1,
                    title = "Buy groceries for the week",
                    description = "Milk, eggs, bread, fruits, vegetables, and some snacks for the weekend.",
                    isCompleted = false,
                    completedAt = null,
                    createdAt = System.currentTimeMillis() - 86400000
                ),
                isLoading = false
            ),
            onIntent = {},
            snackbarHostState = SnackbarHostState()
        )
    }
}

@DevicePreviews
@Composable
private fun TodoDetailContentCompletedPreview() {
    SimpleTodoAppTheme(dynamicColor = false) {
        TodoDetailContent(
            state = TodoDetailState(
                todo = Todo(
                    id = 2,
                    title = "Finish project report",
                    description = "Complete the quarterly report with all the data analysis and charts.",
                    isCompleted = true,
                    completedAt = System.currentTimeMillis(),
                    createdAt = System.currentTimeMillis() - 172800000
                ),
                isLoading = false
            ),
            onIntent = {},
            snackbarHostState = SnackbarHostState()
        )
    }
}

@DevicePreviews
@Composable
private fun TodoDetailContentLoadingPreview() {
    SimpleTodoAppTheme(dynamicColor = false) {
        TodoDetailContent(
            state = TodoDetailState(isLoading = true),
            onIntent = {},
            snackbarHostState = SnackbarHostState()
        )
    }
}
