package com.mobility.race.ui.drawUtils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.mobility.race.domain.GameCamera
import com.mobility.race.domain.GameMap
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min


fun DrawScope.drawBackgroundTexture(
    gameMap: GameMap,
    gameCamera: GameCamera,
    backgroundTexture: ImageBitmap
) {
    val (_, zoom) = gameCamera.getViewMatrix()
    val baseCellSize = min(size.width, size.height) / gameMap.width.toFloat()
    val scaledCellSize = baseCellSize * zoom
    val cellSizePx = Size(scaledCellSize, scaledCellSize)

    val visibleWorldLeft = gameCamera.position.x - (gameCamera.viewportSize.width / 2) / scaledCellSize
    val visibleWorldRight = gameCamera.position.x + (gameCamera.viewportSize.width / 2) / scaledCellSize
    val visibleWorldTop = gameCamera.position.y - (gameCamera.viewportSize.height / 2) / scaledCellSize
    val visibleWorldBottom = gameCamera.position.y + (gameCamera.viewportSize.height / 2) / scaledCellSize

    val mapLeft = 0f
    val mapRight = gameMap.width.toFloat()
    val mapTop = 0f
    val mapBottom = gameMap.height.toFloat()

    if (visibleWorldLeft < mapLeft) {
        val startX = maxOf(visibleWorldLeft, -100f)
        val endX = minOf(mapLeft, visibleWorldRight)

        for (x in floor(startX).toInt() until ceil(endX).toInt()) {
            for (y in floor(visibleWorldTop).toInt() until ceil(visibleWorldBottom).toInt()) {
                val worldPos = Offset(x.toFloat(), y.toFloat())
                val screenPos = gameCamera.worldToScreen(worldPos)
                drawImageBitmap(backgroundTexture, screenPos, cellSizePx)
            }
        }
    }

    if (visibleWorldRight > mapRight) {
        val startX = maxOf(mapRight, visibleWorldLeft)
        val endX = minOf(visibleWorldRight, mapRight + 100f)

        for (x in floor(startX).toInt() until ceil(endX).toInt()) {
            for (y in floor(visibleWorldTop).toInt() until ceil(visibleWorldBottom).toInt()) {
                val worldPos = Offset(x.toFloat(), y.toFloat())
                val screenPos = gameCamera.worldToScreen(worldPos)
                drawImageBitmap(backgroundTexture, screenPos, cellSizePx)
            }
        }
    }

    if (visibleWorldTop < mapTop) {
        val startY = maxOf(visibleWorldTop, -100f)
        val endY = minOf(mapTop, visibleWorldBottom)

        for (x in floor(visibleWorldLeft).toInt() until ceil(visibleWorldRight).toInt()) {
            for (y in floor(startY).toInt() until ceil(endY).toInt()) {
                val worldPos = Offset(x.toFloat(), y.toFloat())
                val screenPos = gameCamera.worldToScreen(worldPos)
                drawImageBitmap(backgroundTexture, screenPos, cellSizePx)
            }
        }
    }

    if (visibleWorldBottom > mapBottom) {
        val startY = maxOf(mapBottom, visibleWorldTop)
        val endY = minOf(visibleWorldBottom, mapBottom + 100f)

        for (x in floor(visibleWorldLeft).toInt() until ceil(visibleWorldRight).toInt()) {
            for (y in floor(startY).toInt() until ceil(endY).toInt()) {
                val worldPos = Offset(x.toFloat(), y.toFloat())
                val screenPos = gameCamera.worldToScreen(worldPos)
                drawImageBitmap(backgroundTexture, screenPos, cellSizePx)
            }
        }
    }
}