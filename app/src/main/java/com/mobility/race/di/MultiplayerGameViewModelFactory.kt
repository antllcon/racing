package com.mobility.race.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.mobility.race.data.Gateway
import com.mobility.race.data.IGateway
import com.mobility.race.presentation.multiplayer.MultiplayerGameViewModel
import io.ktor.client.HttpClient

class MultiplayerGameViewModelFactory(
    private val playerId: String,
    private val playerName: String,
    private val carSpriteId: String,
    private val gateway: IGateway
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(MultiplayerGameViewModel::class.java)) {

            return MultiplayerGameViewModel(
                playerId,
                playerName,
                carSpriteId,
                gateway
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}