package com.lutukai.simpletodoapp.ui.features.tododetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lutukai.simpletodoapp.R
import com.lutukai.simpletodoapp.domain.models.Todo
import com.lutukai.simpletodoapp.ui.components.molecules.KeyValueRow
import com.lutukai.simpletodoapp.ui.components.molecules.LabeledSwitch
import com.lutukai.simpletodoapp.ui.components.molecules.ModalIconHeader
import com.lutukai.simpletodoapp.ui.components.molecules.SectionHeader
import com.lutukai.simpletodoapp.ui.components.molecules.primaryIconButton
import com.lutukai.simpletodoapp.ui.components.molecules.secondaryIconButton
import com.lutukai.simpletodoapp.ui.mvi.ObserveAsEvents
import com.lutukai.simpletodoapp.ui.mvi.collectState
import com.lutukai.simpletodoapp.ui.mvi.rememberOnIntent
import com.lutukai.simpletodoapp.ui.preview.DevicePreviews
import com.lutukai.simpletodoapp.ui.theme.SimpleTodoAppTheme
import com.lutukai.simpletodoapp.ui.util.TestTags
import com.lutukai.simpletodoapp.ui.util.showSnackbarWithAction
import com.lutukai.simpletodoapp.util.Constants
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
                snackbarHostState.showSnackbarWithAction(message = effect.message)
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
    val dateFormat = remember { SimpleDateFormat(Constants.DATE_FORMAT_PATTERN, Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        // Toolbar - using reusable ModalIconHeader
        ModalIconHeader(
            leadingIcon = primaryIconButton(
                icon = Icons.Default.Edit,
                onClick = { onIntent(TodoDetailIntent.EditClicked) },
                contentDescription = stringResource(R.string.cd_edit_task),
                modifier = Modifier.testTag(TestTags.DETAIL_EDIT_BUTTON)
            ),
            trailingIcon = secondaryIconButton(
                icon = Icons.Default.Close,
                onClick = { onIntent(TodoDetailIntent.Dismiss) },
                contentDescription = stringResource(R.string.cd_close),
                modifier = Modifier.testTag(TestTags.DETAIL_CLOSE_BUTTON)
            )
        )

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

                // Completion Toggle - using reusable LabeledSwitch
                LabeledSwitch(
                    label = stringResource(R.string.mark_complete),
                    checked = todo.isCompleted,
                    onCheckedChange = { onIntent(TodoDetailIntent.ToggleComplete(it)) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    switchModifier = Modifier.testTag(TestTags.DETAIL_COMPLETED_SWITCH)
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Notes Section - using reusable SectionHeader
                SectionHeader(title = stringResource(R.string.notes))

                Text(
                    text = todo.description.ifEmpty { "-" },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .testTag(TestTags.DETAIL_NOTES),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )

                HorizontalDivider(modifier = Modifier.padding(16.dp))

                // Metadata - using reusable KeyValueRow
                KeyValueRow(
                    label = stringResource(R.string.created_on),
                    value = todo.createdAt?.let { dateFormat.format(Date(it)) } ?: "-"
                )

                if (todo.isCompleted && todo.completedAt != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    KeyValueRow(
                        label = stringResource(R.string.completed_on),
                        value = dateFormat.format(Date(todo.completedAt))
                    )
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
                    createdAt = System.currentTimeMillis() - 86_400_000
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
                    createdAt = System.currentTimeMillis() - 172_800_000
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
