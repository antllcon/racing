package com.mobility.race.ui.drawUtils

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.mobility.race.domain.Car
import com.mobility.race.presentation.singleplayer.SingleplayerGameState
import kotlin.math.min

fun DrawScope.drawMinimap(
    state: SingleplayerGameState,
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
        innerMinimapSize.width / state.gameMap.width,
        innerMinimapSize.height / state.gameMap.height
    )

    for (y in 0 until state.gameMap.height) {
        for (x in 0 until state.gameMap.width) {
            val terrain = state.gameMap.getTerrainType(x, y)
            val color = when (terrain.uppercase()) {
                "GRASS" -> Color(0xFF4CAF50)
                "ABYSS" -> Color(0xFF2196F3)
                "ROAD" -> Color(0xFF795548)
                else -> Color.Gray
            }

            drawRect(
                color = color,
                topLeft = Offset(
                    innerMinimapPosition.x + x * cellSize,
                    innerMinimapPosition.y + y * cellSize
                ),
                size = Size(cellSize, cellSize)
            )
        }
    }

    drawCircle(
        color = Color.Blue,
        center = Offset(
            innerMinimapPosition.x + state.car.position.x * cellSize,
            innerMinimapPosition.y + state.car.position.y * cellSize
        ),
        radius = cellSize * 0.5f
    )

    drawRoundRect(
        color = strokeColor,
        topLeft = minimapPosition,
        size = minimapSize,
        cornerRadius = CornerRadius(cornerRadius),
        style = Stroke(width = strokeWidth)
    )

}