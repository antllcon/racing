package com.mobility.race.ui.drawUtils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import com.mobility.race.domain.GameCamera
import com.mobility.race.domain.GameMap
import kotlin.math.min

fun DrawScope.drawGameMap(gameMap: GameMap, gameCamera: GameCamera, canvasSize: Size, bitmapStorage: Map<String, ImageBitmap>) {
    val (_, zoom) = gameCamera.getViewMatrix()

    val baseCellSize = min(canvasSize.width, canvasSize.height) / gameMap.width.toFloat()
    val scaledCellSize = baseCellSize * zoom
    val cellSizePx = Size(scaledCellSize, scaledCellSize)

    for (y in 0 until gameMap.height) {
        if ((y + 1) * scaledCellSize < gameCamera.position.y * scaledCellSize - gameCamera.viewportSize.height / 2) {
            continue
        }

        for (x in 0 until gameMap.width) {
            if ((x + 1) * scaledCellSize < gameCamera.position.x * scaledCellSize - gameCamera.viewportSize.width / 2) {
                continue
            }

            val imageName = gameMap.getTerrainName(x, y)
            val terrainType = gameMap.getTerrainAt(x, y)

            val fillColor = when (terrainType) {
                GameMap.TerrainType.ABYSS -> Color.Blue.copy(alpha = 0.7f)
                GameMap.TerrainType.GRASS -> Color(0xFF4CAF50)
                GameMap.TerrainType.ROAD -> Color(0xFF616161)
            }

            val worldPos = Offset(x.toFloat(), y.toFloat())
            val screenPos = gameCamera.worldToScreen(worldPos)

            drawImageBitmap(
                bitmapStorage[imageName]!!,
                screenPos,
                cellSizePx
            )

            if ((x + 1) * scaledCellSize > gameCamera.position.x * scaledCellSize + gameCamera.viewportSize.width / 2) {
                break
            }
        }

        if ((y + 1) * scaledCellSize > gameCamera.position.y * scaledCellSize + gameCamera.viewportSize.height / 2) {
            break
        }
    }
}
