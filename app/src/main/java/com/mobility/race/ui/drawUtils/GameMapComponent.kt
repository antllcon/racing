package com.mobility.race.ui.drawUtils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.mobility.race.domain.GameCamera
import com.mobility.race.domain.GameMap
import kotlin.math.min

fun DrawScope.drawGameMap(gameMap: GameMap, gameCamera: GameCamera, canvasSize: Size) {
    val (_, zoom) = gameCamera.getViewMatrix()

    val baseCellSize = min(canvasSize.width, canvasSize.height) / gameMap.width.toFloat()
    val scaledCellSize = baseCellSize * zoom

    val cellSizePx = Size(scaledCellSize, scaledCellSize)

    for (y in 0 until gameMap.height) {
        for (x in 0 until gameMap.width) {
            val terrainType = gameMap.getTerrainAt(x, y)

            val fillColor = when (terrainType) {
                GameMap.TerrainType.ABYSS -> Color.Blue.copy(alpha = 0.7f)
                GameMap.TerrainType.WATER -> Color.Blue.copy(alpha = 0.7f)
                GameMap.TerrainType.GRASS -> Color(0xFF4CAF50)
                GameMap.TerrainType.ROAD -> Color(0xFF616161)
            }

            val worldPos = Offset(x.toFloat(), y.toFloat())
            val screenPos = gameCamera.worldToScreen(worldPos)

            drawRect(
                color = fillColor,
                topLeft = screenPos,
                size = cellSizePx
            )

            drawRect(
                color = Color.Black.copy(alpha = 0.3f),
                topLeft = screenPos,
                size = cellSizePx,
                style = Stroke(1f)
            )
        }
    }
}
