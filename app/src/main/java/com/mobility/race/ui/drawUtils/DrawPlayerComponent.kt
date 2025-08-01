package com.mobility.race.ui.drawUtils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import com.mobility.race.domain.Car
import com.mobility.race.presentation.multiplayer.MultiplayerGameState
import kotlin.math.PI

fun DrawScope.drawCars(state: MultiplayerGameState, bitmaps: Map<String, ImageBitmap>) {
    state.players.forEach { player ->
        if (!player.isFinished) {
            rotate(
                degrees = player.car.visualDirection * (180f / PI.toFloat()) + 90,
                pivot = state.gameCamera.worldToScreen(player.car.position)
            ) {
                drawImageBitmap(
                    bitmaps["car" + player.car.id + "_" + player.car.currentSprite]!!,
                    Offset(
                        state.gameCamera.worldToScreen(player.car.position).x - Car.LENGTH * state.gameCamera.getScaledCellSize(
                            state.gameMap.size
                        ) / 2,
                        state.gameCamera.worldToScreen(player.car.position).y - Car.WIDTH * state.gameCamera.getScaledCellSize(
                            state.gameMap.size
                        ) / 2
                    ),
                    Size(
                        Car.LENGTH * state.gameCamera.getScaledCellSize(state.gameMap.size),
                        Car.WIDTH * state.gameCamera.getScaledCellSize(state.gameMap.size)
                    )
                )
            }
        }
    }
}