package com.lutukai.simpletodoapp.ui.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.withContext

/**
 * Collects UI state with lifecycle awareness
 */
@Composable
fun <S : UiState> MviViewModel<S, *, *>.collectState(): S {
    val state by state.collectAsStateWithLifecycle()
    return state
}


/**
 * Creates a stable event channel for dispatching events.
 * Returns a stable lambda reference that won't cause recompositions.
 */
@Composable
fun <T> rememberEventChannel(
    onEvent: (T) -> Unit
): (T) -> Unit {
    val channel = remember { Channel<T>(Channel.BUFFERED) }
    val currentHandler by rememberUpdatedState(onEvent)

    LaunchedEffect(Unit) {
        channel.consumeAsFlow().collect { currentHandler(it) }
    }

    return remember { { event: T -> channel.trySend(event) } }
}

/**
 * Convenience extension for MviViewModel to get a stable intent handler.
 */
@Composable
fun <S : UiState, I : UiIntent, E : SideEffect> MviViewModel<S, I, E>.rememberOnIntent(): (I) -> Unit {
    return rememberEventChannel { intent -> onIntent(intent) }
}


@Composable
fun <T> ObserveAsEvents(
    flow: Flow<T>,
    key1: Any? = null,
    key2: Any? = null,
    onEvent: suspend (T) -> Unit
){
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner, key1, key2) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
            withContext(Dispatchers.Main.immediate){
                flow.collect { onEvent(it) }
            }
        }
    }
}

