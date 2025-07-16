package com.mobility.race.data

interface IGateway {
    suspend fun connect()
    suspend fun disconnect()
    suspend fun initPlayer(name: String)
    suspend fun createRoom(name: String)
    suspend fun joinRoom(name: String)
    suspend fun leaveRoom()
    suspend fun playerAction(name: PlayerInputRequest)
}