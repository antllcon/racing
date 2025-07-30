package com.mobility.race.data

import androidx.compose.ui.geometry.Offset
import com.mobility.race.domain.GameMap
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

interface IGateway {
    val messageFlow: Flow<ServerMessage>

    fun fillGatewayStorage(starterPack: StarterPack)
    fun openGatewayStorage(): StarterPack
    suspend fun connect()
    suspend fun disconnect()
    suspend fun initPlayer(name: String)
    suspend fun createRoom(name: String)
    suspend fun joinRoom(name: String)
    suspend fun startGame(name: String)
    suspend fun leaveRoom()
    suspend fun playerAction(name: PlayerInputRequest)
    suspend fun playerFinished(name: String)
}