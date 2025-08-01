package com.mobility.race.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

abstract class BaseViewModel<TState : Any>(
    initialState: TState
) : ViewModel() {

    val state: State<TState> by lazy { mState }

    private val mState = mutableStateOf(initialState)

    protected val stateValue
        get() = mState.value

    protected fun modifyState(modifier: TState.() -> TState) {
        mState.value = mState.value.modifier()
    }
}