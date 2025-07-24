package com.mobility.race.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.mobility.race.presentation.multiplayer.MultiplayerGameViewModel
import com.mobility.race.presentation.multiplayer.RoomViewModel
import io.ktor.client.HttpClient

class RoomViewModelFactory(
    private val httpClient: HttpClient
): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(MultiplayerGameViewModel::class.java)) {
            val savedStateHandle = extras.createSavedStateHandle()

            println(" ===== фабрика")

            return RoomViewModel(
                savedStateHandle = savedStateHandle,
                httpClient = httpClient
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}