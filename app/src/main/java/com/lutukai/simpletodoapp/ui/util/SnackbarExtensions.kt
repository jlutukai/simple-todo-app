package com.lutukai.simpletodoapp.ui.util

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult

suspend fun SnackbarHostState.showSnackbarWithAction(
    message: String,
    actionLabel: String? = null,
    duration: SnackbarDuration = SnackbarDuration.Short,
    onActionPerformed: (() -> Unit)? = null
) {
    val result = showSnackbar(
        message = message,
        actionLabel = actionLabel,
        duration = duration
    )
    if (result == SnackbarResult.ActionPerformed) {
        onActionPerformed?.invoke()
    }
}
