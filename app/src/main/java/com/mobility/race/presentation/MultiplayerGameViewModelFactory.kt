package com.mobility.race.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import io.ktor.client.HttpClient

class MultiplayerGameViewModelFactory(
    private val httpClient: HttpClient
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(MultiplayerGameViewModel::class.java)) {
            val savedStateHandle = extras.createSavedStateHandle()
            return MultiplayerGameViewModel(
                savedStateHandle = savedStateHandle,
                httpClient = httpClient
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}