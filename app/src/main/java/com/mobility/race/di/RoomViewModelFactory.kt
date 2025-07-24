package com.mobility.race.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.mobility.race.data.IGateway
import com.mobility.race.presentation.multiplayer.RoomViewModel

class RoomViewModelFactory(
    private val playerName: String,
    private val roomName: String,
    private val isCreatingRoom: Boolean,
    private val navigateToMultiplayerGame: () -> Unit,
    private val gateway: IGateway
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(RoomViewModel::class.java)) {

            return RoomViewModel(
                playerName,
                roomName,
                isCreatingRoom,
                navigateToMultiplayerGame,
                gateway
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}