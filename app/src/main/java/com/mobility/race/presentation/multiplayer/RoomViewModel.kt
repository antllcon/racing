package com.mobility.race.presentation.multiplayer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import io.ktor.client.HttpClient

class RoomViewModel(
    savedStateHandle: SavedStateHandle,
    private val httpClient: HttpClient
) : ViewModel() {
}