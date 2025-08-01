package com.mobility.race.ui.drawUtils

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.mobility.race.domain.Car
import com.mobility.race.domain.CheckpointManager
import com.mobility.race.domain.GameMap
import com.mobility.race.presentation.singleplayer.SingleplayerGameState
import kotlin.math.min

fun DrawScope.drawMinimap(
    map: GameMap,
    car: Car,
    cars: List<Car>,
    isFinished: Boolean,
    checkpointManager: CheckpointManager
) {
    val minimapSize = Size(300f, 300f)
    val minimapPosition = Offset(50f, 50f)
    val strokeWidth = 8f
    val strokeColor = Color.Gray
    val cornerRadius = 8f

    drawRoundRect(
        color = Color.Black.copy(alpha = 0.7f),
        topLeft = minimapPosition,
        size = minimapSize,
        cornerRadius = CornerRadius(cornerRadius)
    )

    val innerMinimapPosition = Offset(
        minimapPosition.x + strokeWidth,
        minimapPosition.y + strokeWidth
    )
    val innerMinimapSize = Size(
        minimapSize.width - strokeWidth * 2,
        minimapSize.height - strokeWidth * 2
    )

    val cellSize = min(
        innerMinimapSize.width / map.width,
        innerMinimapSize.height / map.height
    )

    val nextCheckpoint = checkpointManager.getNextCheckpoint(car.id)

    for (y in 0 until map.height) {
        for (x in 0 until map.width) {
            val terrain = map.getTerrainType(x, y)
            val color = when (terrain.uppercase()) {
                "GRASS" -> Color(0xFF4CAF50)
                "WATER" -> Color(0xFF2196F3)
                "ROAD" -> Color(0xFF795548)
                else -> Color.Gray
            }

            val topLeft = Offset(
                innerMinimapPosition.x + x * cellSize,
                innerMinimapPosition.y + y * cellSize
            )

            if (nextCheckpoint != null && x.toFloat() == nextCheckpoint.x && y.toFloat() == nextCheckpoint.y && !isFinished) {
                drawRect(
                    color = Color.Red.copy(alpha = 0.7f),
                    topLeft = topLeft,
                    size = Size(cellSize, cellSize)
                )
                drawRect(
                    color = Color.Yellow,
                    topLeft = topLeft,
                    size = Size(cellSize, cellSize),
                    style = Stroke(width = 2f)
                )
            } else {
                drawRect(
                    color = color,
                    topLeft = topLeft,
                    size = Size(cellSize, cellSize)
                )
            }
        }
    }

    for (player in cars) {
        drawCircle(
            color = (if (player == car) Color.Blue else Color.Red),
            center = Offset(
                innerMinimapPosition.x + player.position.x * cellSize,
                innerMinimapPosition.y + player.position.y * cellSize
            ),
            radius = cellSize * 0.5f
        )
    }


    drawRoundRect(
        color = strokeColor,
        topLeft = minimapPosition,
        size = minimapSize,
        cornerRadius = CornerRadius(cornerRadius),
        style = Stroke(width = strokeWidth)
    )
}