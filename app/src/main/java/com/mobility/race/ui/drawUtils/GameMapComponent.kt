package com.mobility.race.ui.drawUtils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
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

            val worldPos = Offset(x.toFloat(), y.toFloat())
            val screenPos = gameCamera.worldToScreen(worldPos)
//            println(imageName)

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
