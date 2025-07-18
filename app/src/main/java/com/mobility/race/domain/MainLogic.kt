package com.mobility.race.domain

import com.mobility.race.presentation.IGameplay

class MainLogic(private val viewModel: IGameplay) {
    fun handleGameCycle(elapsedTime: Float) {
        viewModel.movePlayer(elapsedTime)
        viewModel.moveCamera(elapsedTime)
    }
}