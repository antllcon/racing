package com.mobility.race.ui.drawUtils

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.mobility.race.domain.Bonus
import com.mobility.race.domain.GameCamera

fun DrawScope.drawBonuses(
    bonuses: List<Bonus>,
    camera: GameCamera,
    bitmaps: Map<String, ImageBitmap>
) {
    bonuses.forEach { bonus ->
        if (bonus.isActive) {
            val screenPos = camera.worldToScreen(bonus.position)
            val cellSize = camera.getScaledCellSize(camera.mapWidth)
            val bonusSize = cellSize * 0.8f

            val bitmap = when (bonus.type) {
                Bonus.BonusType.SPEED_BOOST -> bitmaps["bonus_speed"]
                Bonus.BonusType.MASS_INCREASE -> bitmaps["bonus_mass"]
            }

            bitmap?.let {
                drawImage(
                    image = it,
                    dstSize = IntSize(bonusSize.toInt(), bonusSize.toInt()),
                    dstOffset = IntOffset(
                        (screenPos.x - bonusSize / 2).toInt(),
                        (screenPos.y - bonusSize / 2).toInt()
                    )
                )
            }
        }
    }
}