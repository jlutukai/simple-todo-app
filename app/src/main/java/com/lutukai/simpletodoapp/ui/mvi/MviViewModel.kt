package com.lutukai.simpletodoapp.ui.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Base MVI ViewModel that provides a unidirectional data flow pattern.
 *
 * @param State The UI state type
 * @param Intent The user intent/action type
 * @param Effect The side effect type
 */
abstract class MviViewModel<State : UiState, Intent : UiIntent, Effect : SideEffect>(initialState: State) :
    ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _effect = Channel<Effect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    protected val currentState: State
        get() = _state.value

    /**
     * Process user intents and update state accordingly
     */
    fun onIntent(intent: Intent) {
        viewModelScope.launch {
            handleIntent(intent)
        }
    }

    /**
     * Override to handle specific intents
     */
    protected abstract suspend fun handleIntent(intent: Intent)

    /**
     * Update the current state using a reducer function
     */
    protected fun updateState(reducer: State.() -> State) {
        _state.value = currentState.reducer()
    }

    /**
     * Send a side effect to the UI
     */
    protected fun sendEffect(effect: Effect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
