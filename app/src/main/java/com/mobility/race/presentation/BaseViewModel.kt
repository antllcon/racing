package com.mobility.race.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

abstract class BaseViewModel<TState : Any>(
    initialState: TState
) : ViewModel() {

    private val _state = mutableStateOf(initialState)
    val state: State<TState> = _state

    protected val stateValue: TState
        get() = _state.value

    protected fun modifyState(transform: (TState) -> TState) {
        _state.value = transform(_state.value)
    }
}
