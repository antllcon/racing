package com.mobility.race.presentation

import androidx.compose.ui.geometry.Offset
import com.mobility.race.domain.Car
import com.mobility.race.domain.GameCamera
import com.mobility.race.domain.GameMap

interface IGameplay {
    fun init(playerCar: Car, playerGameMap: GameMap, playerCamera: GameCamera)
    fun runGame()
    fun stopGame()
    fun movePlayer(touchCoordinates: Offset)
}