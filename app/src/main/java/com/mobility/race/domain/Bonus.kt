package com.mobility.race.domain

import androidx.compose.ui.geometry.Offset
import kotlin.random.Random

sealed class Bonus(
    open val position: Offset,
    val type: BonusType,
    val duration: Float = 5f, 
    open val isActive: Boolean = true
) {
    enum class BonusType {
        SPEED_BOOST,
        MASS_INCREASE
    }

    data class SpeedBoost(
        override val position: Offset,
        override val isActive: Boolean = true
    ) : Bonus(position, BonusType.SPEED_BOOST, 5f, isActive)

    data class MassIncrease(
        override val position: Offset,
        override val isActive: Boolean = true
    ) : Bonus(position, BonusType.MASS_INCREASE, 5f, isActive)

    companion object {
        fun generateRandomBonus(position: Offset): Bonus {
            return when (Random.nextInt(2)) {
                0 -> SpeedBoost(position)
                else -> MassIncrease(position)
            }
        }
    }
}