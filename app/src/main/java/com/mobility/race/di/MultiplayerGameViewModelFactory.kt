package com.mobility.race.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.navigation.NavController
import com.mobility.race.data.Gateway
import com.mobility.race.data.IGateway
import com.mobility.race.presentation.multiplayer.MultiplayerGameViewModel
import io.ktor.client.HttpClient

class MultiplayerGameViewModelFactory(
    private val nickname: String,
    private val playerNames: Array<String>,
    private val carSpriteId: String,
    private val navController: NavController,
    private val gateway: IGateway
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(MultiplayerGameViewModel::class.java)) {

            return MultiplayerGameViewModel(
                nickname,
                playerNames,
                carSpriteId,
                navController,
                gateway
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}