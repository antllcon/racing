package com.mobility.race.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mobility.race.data.IGateway
import com.mobility.race.presentation.multiplayer.MultiplayerGameViewModel

class MultiplayerGameViewModelFactory(
    private val playerId: String,
    private val playerNames: Array<String>,
    private val carSpriteId: String,
    private val gateway: IGateway
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MultiplayerGameViewModel(
            playerId,
            playerNames,
            carSpriteId,
            gateway
        ) as T
    }
}