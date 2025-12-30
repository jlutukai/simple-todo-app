package com.lutukai.simpletodoapp.ui.addedittodo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lutukai.simpletodoapp.ui.util.TestTags
import com.lutukai.simpletodoapp.R
import com.lutukai.simpletodoapp.ui.mvi.ObserveAsEvents
import com.lutukai.simpletodoapp.ui.mvi.collectState
import com.lutukai.simpletodoapp.ui.mvi.rememberOnIntent
import com.lutukai.simpletodoapp.ui.preview.DevicePreviews
import com.lutukai.simpletodoapp.ui.theme.SimpleTodoAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTodoScreen(
    todoId: Long?,
    onDismiss: () -> Unit,
    viewModel: AddEditTodoMviViewModel = hiltViewModel()
) {
    val state = viewModel.collectState()
    val onIntent = viewModel.rememberOnIntent()
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(todoId) {
        todoId?.let { viewModel.onIntent(AddEditTodoIntent.LoadTodo(it)) }
    }

    ObserveAsEvents(flow = viewModel.effect) { effect ->
        when (effect) {
            is AddEditTodoSideEffect.SaveSuccess -> onDismiss()
            is AddEditTodoSideEffect.ShowError -> {
                snackbarHostState.showSnackbar(effect.message)
            }
            is AddEditTodoSideEffect.Dismiss -> onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = Color.Black.copy(alpha = 0.32f)
    ) {
        AddEditTodoContent(
            state = state,
            onIntent = onIntent,
            snackbarHostState = snackbarHostState
        )
    }
}

@Composable
internal fun AddEditTodoContent(
    state: AddEditTodoState,
    onIntent: (AddEditTodoIntent) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { onIntent(AddEditTodoIntent.Cancel) },
                modifier = Modifier.testTag(TestTags.ADD_EDIT_CANCEL_BUTTON)
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = stringResource(
                    if (state.isEditMode) R.string.edit_todo_title else R.string.add_todo_title
                ),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(48.dp)) // Balance the cancel button
        }

        // Form Fields
        Column(modifier = Modifier.padding(16.dp)) {
            // Title
            Text(
                text = stringResource(R.string.title_label),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = state.title,
                onValueChange = { onIntent(AddEditTodoIntent.UpdateTitle(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestTags.ADD_EDIT_TITLE_FIELD),
                placeholder = {
                    Text(
                        stringResource(R.string.title_placeholder),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Text(
                text = stringResource(R.string.description_label),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = state.description,
                onValueChange = { onIntent(AddEditTodoIntent.UpdateDescription(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp)
                    .testTag(TestTags.ADD_EDIT_DESCRIPTION_FIELD),
                placeholder = {
                    Text(
                        stringResource(R.string.description_placeholder),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Completed Toggle (only in edit mode)
            if (state.isEditMode) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.completed_label),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = state.isCompleted,
                        onCheckedChange = { onIntent(AddEditTodoIntent.UpdateCompleted(it)) },
                        modifier = Modifier.testTag(TestTags.ADD_EDIT_COMPLETED_SWITCH)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Save Button
        Button(
            onClick = { onIntent(AddEditTodoIntent.SaveTodo) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp)
                .testTag(TestTags.ADD_EDIT_SAVE_BUTTON),
            enabled = state.isSaveEnabled,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = stringResource(R.string.save),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}

@DevicePreviews
@Composable
private fun AddTodoContentPreview() {
    SimpleTodoAppTheme(dynamicColor = false) {
        AddEditTodoContent(
            state = AddEditTodoState(
                title = "",
                description = "",
                isEditMode = false,
                isLoading = false
            ),
            onIntent = {},
            snackbarHostState = SnackbarHostState()
        )
    }
}

@DevicePreviews
@Composable
private fun AddTodoContentFilledPreview() {
    SimpleTodoAppTheme(dynamicColor = false) {
        AddEditTodoContent(
            state = AddEditTodoState(
                title = "Buy groceries",
                description = "Milk, eggs, bread, fruits",
                isEditMode = false,
                isLoading = false
            ),
            onIntent = {},
            snackbarHostState = SnackbarHostState()
        )
    }
}

@DevicePreviews
@Composable
private fun EditTodoContentPreview() {
    SimpleTodoAppTheme(dynamicColor = false) {
        AddEditTodoContent(
            state = AddEditTodoState(
                title = "Finish project report",
                description = "Complete the quarterly report with all the data analysis",
                isEditMode = true,
                isCompleted = false,
                isLoading = false
            ),
            onIntent = {},
            snackbarHostState = SnackbarHostState()
        )
    }
}

@DevicePreviews
@Composable
private fun AddTodoContentLoadingPreview() {
    SimpleTodoAppTheme(dynamicColor = false) {
        AddEditTodoContent(
            state = AddEditTodoState(
                title = "Buy groceries",
                description = "Milk, eggs, bread",
                isEditMode = false,
                isLoading = true
            ),
            onIntent = {},
            snackbarHostState = SnackbarHostState()
        )
    }
}
