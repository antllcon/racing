package com.mobility.race.presentation

import androidx.compose.ui.geometry.Offset
import com.mobility.race.domain.Car
import com.mobility.race.domain.Camera
import com.mobility.race.domain.Map

interface IGameplay {
    fun init(playerCar: Car, playerGameMap: Map, playerCamera: Camera)
    fun runGame()
    fun stopGame()
    fun movePlayer(touchCoordinates: Offset)
}