package com.mobility.race.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.navigation.NavController
import com.mobility.race.data.IGateway
import com.mobility.race.presentation.multiplayer.RoomViewModel

class RoomViewModelFactory(
    private val playerName: String,
    private val roomName: String,
    private val isCreatingRoom: Boolean,
    private val context: Context,
    private val navController: NavController,
    private val gateway: IGateway
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(RoomViewModel::class.java)) {

            return RoomViewModel(
                playerName,
                roomName,
                isCreatingRoom,
                context,
                navController,
                gateway
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}