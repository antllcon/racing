package com.mobility.race.ui.drawUtils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.mobility.race.domain.Bonus
import com.mobility.race.domain.GameCamera
import kotlin.math.sin

fun DrawScope.drawBonuses(
    bonuses: List<Bonus>,
    camera: GameCamera,
    bitmaps: Map<String, ImageBitmap>,
    mapWidth: Int,
    mapHeight: Int
) {
    val scaledCellSize = camera.getScaledCellSize(mapWidth)
    val cellSizePx = Size(scaledCellSize, scaledCellSize)
    val currentTime = System.currentTimeMillis()

    bonuses.forEach { bonus ->
        if (bonus.isActive) {
            val screenPos = camera.worldToScreen(bonus.position)

            println("DRAWING BONUS: id=${bonus}, type=${bonus.type}")
            println("  World Pos: (${bonus.position.x}, ${bonus.position.y})")
            println("  Camera Pos: (${camera.position.x}, ${camera.position.y})")
            println("  Screen Pos: (${screenPos.x}, ${screenPos.y})")
            println("  Viewport Size: (${camera.viewportSize.width}, ${camera.viewportSize.height})")

            val bonusSize = Size(cellSizePx.width / 5f, cellSizePx.height / 5f)
            val centerPos = screenPos + Offset(bonusSize.width / 2, bonusSize.height / 2)

            val pulse = sin(currentTime / 200f) * 0.1f
            val scale = (1f + pulse)

            withTransform({
                scale(scale, scale, centerPos)
            }) {
                val bitmap = when (bonus.type) {
                    Bonus.BonusType.SPEED_BOOST -> bitmaps["bonus_speed"]
                    Bonus.BonusType.MASS_INCREASE -> bitmaps["bonus_size"]
                }
                bitmap?.let { bonusTexture ->
                    drawImage(
                        image = bonusTexture,
                        dstOffset = IntOffset(
                            (screenPos.x - bonusSize.width / 2).toInt(),
                            (screenPos.y - bonusSize.height / 2).toInt()
                        ),
                        dstSize = IntSize(bonusSize.width.toInt(), bonusSize.height.toInt())
                    )
                }
            }

            if (bonus.type == Bonus.BonusType.SPEED_BOOST) {
                val glowSizeFactor = 1.5f * (1 + sin(currentTime / 250f) * 0.1f)
                val glowSize = Size(
                    width = bonusSize.width * glowSizeFactor,
                    height = bonusSize.height * glowSizeFactor
                )

                bitmaps["glow_effect"]?.let { glowTexture ->
                    drawImage(
                        image = glowTexture,
                        dstOffset = IntOffset(
                            (screenPos.x - (glowSize.width / 2)).toInt(),
                            (screenPos.y - (glowSize.height / 2)).toInt()
                        ),
                        dstSize = IntSize(glowSize.width.toInt(), glowSize.height.toInt())
                    )
                }
            }
        } else {
        println("SKIPPING INACTIVE BONUS: id=${bonus}")
        }
    }
    if (bonuses.isEmpty()) {
        println("NO BONUSES TO DRAW.")
    }
}