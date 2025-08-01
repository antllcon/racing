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
            val camera = state.gameCamera
            val cellSize = camera.getScaledCellSize(state.gameMap.size)
            val carPositionOnScreen = camera.worldToScreen(player.car.position)

            val originalWidth = Car.LENGTH * cellSize
            val originalHeight = Car.WIDTH * cellSize

            val newWidth = originalWidth * player.car.sizeModifier
            val newHeight = originalHeight * player.car.sizeModifier

            val offsetX = (newWidth - originalWidth) / 2
            val offsetY = (newHeight - originalHeight) / 2

            rotate(
                degrees = player.car.visualDirection * (180f / PI.toFloat()) + 90,
                pivot = carPositionOnScreen
            ) {
                drawImageBitmap(
                    bitmaps["car" + player.car.id + "_" + player.car.currentSprite]!!,
                    Offset(
                        carPositionOnScreen.x - newWidth / 2,
                        carPositionOnScreen.y - newHeight / 2
                    ),
                    Size(
                        newWidth,
                        newHeight
                    )
                )
            }
        }
    }
}