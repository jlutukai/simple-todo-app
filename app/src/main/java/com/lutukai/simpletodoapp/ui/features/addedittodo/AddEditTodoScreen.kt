package com.lutukai.simpletodoapp.ui.features.addedittodo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lutukai.simpletodoapp.R
import com.lutukai.simpletodoapp.ui.components.atoms.PrimaryButton
import com.lutukai.simpletodoapp.ui.components.molecules.LabeledSwitch
import com.lutukai.simpletodoapp.ui.components.molecules.LabeledTextField
import com.lutukai.simpletodoapp.ui.components.molecules.ModalHeader
import com.lutukai.simpletodoapp.ui.components.molecules.ModalHeaderTextButton
import com.lutukai.simpletodoapp.ui.mvi.ObserveAsEvents
import com.lutukai.simpletodoapp.ui.mvi.collectState
import com.lutukai.simpletodoapp.ui.mvi.rememberOnIntent
import com.lutukai.simpletodoapp.ui.preview.DevicePreviews
import com.lutukai.simpletodoapp.ui.theme.SimpleTodoAppTheme
import com.lutukai.simpletodoapp.ui.util.TestTags
import com.lutukai.simpletodoapp.ui.util.showSnackbarWithAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTodoScreen(todoId: Long?, onDismiss: () -> Unit, viewModel: AddEditTodoMviViewModel = hiltViewModel()) {
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
                snackbarHostState.showSnackbarWithAction(message = effect.message)
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
        // Header - using reusable ModalHeader component
        ModalHeader(
            title = stringResource(
                if (state.isEditMode) R.string.edit_todo_title else R.string.add_todo_title
            ),
            leadingAction = {
                ModalHeaderTextButton(
                    text = stringResource(R.string.cancel),
                    onClick = { onIntent(AddEditTodoIntent.Cancel) },
                    modifier = Modifier.testTag(TestTags.ADD_EDIT_CANCEL_BUTTON)
                )
            }
        )

        // Form Fields
        Column(modifier = Modifier.padding(16.dp)) {
            // Title field - using reusable LabeledTextField
            LabeledTextField(
                label = stringResource(R.string.title_label),
                value = state.title,
                onValueChange = { onIntent(AddEditTodoIntent.UpdateTitle(it)) },
                placeholder = stringResource(R.string.title_placeholder),
                singleLine = true,
                textFieldModifier = Modifier.testTag(TestTags.ADD_EDIT_TITLE_FIELD)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description field - using reusable LabeledTextField
            LabeledTextField(
                label = stringResource(R.string.description_label),
                value = state.description,
                onValueChange = { onIntent(AddEditTodoIntent.UpdateDescription(it)) },
                placeholder = stringResource(R.string.description_placeholder),
                singleLine = false,
                minLines = 3,
                textFieldModifier = Modifier
                    .heightIn(min = 100.dp)
                    .testTag(TestTags.ADD_EDIT_DESCRIPTION_FIELD)
            )

            // Completed Toggle (only in edit mode) - using reusable LabeledSwitch
            if (state.isEditMode) {
                Spacer(modifier = Modifier.height(16.dp))
                LabeledSwitch(
                    label = stringResource(R.string.completed_label),
                    checked = state.isCompleted,
                    onCheckedChange = { onIntent(AddEditTodoIntent.UpdateCompleted(it)) },
                    switchModifier = Modifier.testTag(TestTags.ADD_EDIT_COMPLETED_SWITCH)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Save Button - using reusable PrimaryButton
        PrimaryButton(
            text = stringResource(R.string.save),
            onClick = { onIntent(AddEditTodoIntent.SaveTodo) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag(TestTags.ADD_EDIT_SAVE_BUTTON),
            enabled = state.isSaveEnabled,
            isLoading = state.isLoading
        )

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
