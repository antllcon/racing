package com.mobility.race.ui.drawUtils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import com.mobility.race.domain.GameCamera
import com.mobility.race.domain.GameMap
import kotlin.math.min
import kotlin.math.sin

fun DrawScope.drawGameMap(
    gameMap: GameMap,
    gameCamera: GameCamera,
    canvasSize: Size,
    bitmapStorage: Map<String, ImageBitmap>
) {
    val (_, zoom) = gameCamera.getViewMatrix()
    val baseCellSize = min(canvasSize.width, canvasSize.height) / gameMap.width.toFloat()
    val scaledCellSize = baseCellSize * zoom
    val cellSizePx = Size(scaledCellSize, scaledCellSize)
    val currentTime = System.currentTimeMillis()

    val cameraX = gameCamera.position.x * scaledCellSize
    val cameraY = gameCamera.position.y * scaledCellSize
    val halfViewportWidth = gameCamera.viewportSize.width / 2
    val halfViewportHeight = gameCamera.viewportSize.height / 2

    for (y in 0 until gameMap.height) {
        val cellBottom = (y + 1) * scaledCellSize
        if (cellBottom < cameraY - halfViewportHeight) continue

        for (x in 0 until gameMap.width) {
            val cellRight = (x + 1) * scaledCellSize
            if (cellRight < cameraX - halfViewportWidth) continue

            val imageName = gameMap.getTerrainName(x, y)
            val worldPos = Offset(x.toFloat(), y.toFloat())
            val screenPos = gameCamera.worldToScreen(worldPos)

            bitmapStorage[imageName]?.let { texture ->
                drawImageBitmap(
                    imageBitmap = texture,
                    offset = screenPos,
                    size = cellSizePx
                )
            }

            if (cellRight > cameraX + halfViewportWidth) break
        }

        if (cellBottom > cameraY + halfViewportHeight) break
    }

    gameMap.getBonuses().forEach { bonus ->
        val timeSinceSpawn = currentTime - bonus.spawnTime

        if (!bonus.isActive && timeSinceSpawn > GameMap.Bonus.ANIMATION_DURATION) return@forEach

        val screenPos = gameCamera.worldToScreen(bonus.position)
        // Уменьшаем размер бонуса в 5 раз
        val bonusSize = Size(cellSizePx.width / 5f, cellSizePx.height / 5f)
        val centerPos = screenPos + Offset(bonusSize.width / 2, bonusSize.height / 2)

        val (scale, alpha) = when {
            !bonus.isActive -> {
                val progress = timeSinceSpawn.toFloat() / GameMap.Bonus.ANIMATION_DURATION
                1.5f * (1 - progress) to (1 - progress)
            }
            timeSinceSpawn < GameMap.Bonus.ANIMATION_DURATION -> {
                val progress = timeSinceSpawn.toFloat() / GameMap.Bonus.ANIMATION_DURATION
                (0.5f + progress * 0.5f) to progress
            }
            else -> {
                val pulse = sin(currentTime / 200f) * 0.1f
                (1f + pulse) to 1f
            }
        }

        withTransform({
            scale(scale, scale, centerPos)
            if (bonus.type == GameMap.Bonus.TYPE_SIZE) {
                rotate(15f * sin(currentTime / 300f), centerPos)
            }
        }) {
            bitmapStorage[bonus.type]?.let { bonusTexture ->
                drawImageBitmap(
                    imageBitmap = bonusTexture,
                    offset = screenPos,
                    size = bonusSize // Используем уменьшенный размер
                )
            }
        }

        if (bonus.isActive && bonus.type == GameMap.Bonus.TYPE_SPEED) {
            val glowSizeFactor = 1.5f * (1 + sin(currentTime / 250f) * 0.1f)
            val glowSize = Size(
                width = bonusSize.width * glowSizeFactor,
                height = bonusSize.height * glowSizeFactor
            )

            bitmapStorage["glow_effect"]?.let { glowTexture ->
                drawImageBitmap(
                    imageBitmap = glowTexture,
                    offset = screenPos - Offset(
                        (glowSize.width - bonusSize.width) / 2,
                        (glowSize.height - bonusSize.height) / 2
                    ),
                    size = glowSize
                )
            }
        }
    }
}