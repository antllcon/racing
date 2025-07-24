package com.mobility.race.presentation.multiplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobility.race.data.GameCountdownUpdateResponse
import com.mobility.race.data.IGateway
import com.mobility.race.data.ServerMessage
import com.mobility.race.presentation.BaseViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MultiplayerGameViewModel(
    gateway: IGateway
): BaseViewModel<MultiplayerGameState>(MultiplayerGameState.default()) {

    init {
        gateway.messageFlow
            .onEach(::handleMessage)
            .launchIn(viewModelScope)
    }

    private fun handleMessage(message: ServerMessage) {
        when (message) {
            is GameCountdownUpdateResponse -> {
                modifyState {
                    copy(
                        countdown = message.remainingTime
                    )
                }
            }
            else -> Unit
        }
    }
}