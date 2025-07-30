package com.mobility.race.data

import com.mobility.race.ui.PlayerResult
import kotlinx.coroutines.flow.Flow

interface IGateway {
    val messageFlow: Flow<ServerMessage>

    fun fillStarterGatewayStorage(starterPack: StarterPack)
    fun openStarterGatewayStorage(): StarterPack
    fun fillEnderGatewayStorage(playerResult: List<PlayerResult>)
    fun openEnderGatewayStorage(): List<PlayerResult>
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