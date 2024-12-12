/* $Id$ */
package com.muthuraj.cycle.fill.util

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by Muthuraj on 08/12/24.
 */
abstract class BaseViewModel<Event : ViewEvent, UiState : ViewState> : ViewModel() {
    private val initialState: UiState by lazy { setInitialState() }

    abstract fun setInitialState(): UiState

    protected val mutableViewState by lazy { MutableStateFlow(initialState) }
    val viewState: StateFlow<UiState> by lazy { mutableViewState }

    private val eventSharedFlow: MutableSharedFlow<Event> = MutableSharedFlow()

    init {
        subscribeToEvents()
    }

    fun setEvent(event: Event) {
        viewModelScope.launch { eventSharedFlow.emit(event) }
    }

    protected inline fun setState(reducer: UiState.() -> UiState) {
        mutableViewState.update { it.reducer() }
        logV { "viewState: ${mutableViewState.value}" }
    }

    private fun subscribeToEvents() {
        eventSharedFlow
            .onEach {
                logV { "handleEvents() called with $it" }
                handleEvents(it)
            }.launchIn(viewModelScope)
    }

    protected abstract fun handleEvents(event: Event)
}

@Stable
interface ViewState

interface ViewEvent