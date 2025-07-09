package com.mobility.race.domain

import androidx.compose.ui.geometry.Offset
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class Car(
    val playerName: String = "Player",
    var isPlayer: Boolean = true,
    var isMultiplayer: Boolean = false,
    var numberPlayer: Int = 1
) {
    companion object {
        const val MIN_SPEED = 0f
        const val MAX_SPEED = 3f
        const val ACCELERATION_RATE = 0.5f
        const val DECELERATION_RATE = 0.8f
        const val TURN_RATE = 1.2f
        const val DRIFT_FACTOR = 0.85f
        const val SIZE = 100f
        const val TURN_ANIMATION_SPEED = 0.05f
    }

    // Current state
    private var speed: Float = 0f
    private var directionRad: Float = 0f
    private var visualDirectionRad: Float = 0f
    var position: Offset = Offset(0f, 0f)
    private var isAlive: Boolean = true
    private var turnDirection: Float = 0f
    private var isDrifting: Boolean = false
    private var speedModifier: Float = 1.0f

    fun setSpeedModifier(modifier: Float) {
        speedModifier = modifier.coerceIn(0f, 1f)
    }

    fun accelerate(deltaTime: Float) {
        if (!isAlive) return
        speed = min(speed + ACCELERATION_RATE * deltaTime * speedModifier, MAX_SPEED * speedModifier)
    }

    fun decelerate(deltaTime: Float) {
        if (!isAlive) return
        speed = max(speed - DECELERATION_RATE * deltaTime * speedModifier, MIN_SPEED)
    }

    fun startTurn(direction: Float) {
        turnDirection = direction.coerceIn(-1f, 1f)
    }

    fun stopTurn() {
        turnDirection = 0f
    }

    fun update(deltaTime: Float) {
        if (!isAlive) return

        // Handle turning
        if (speed > 0 && turnDirection != 0f) {
            val turnAmount = turnDirection * TURN_RATE * deltaTime * (speed / (MAX_SPEED * speedModifier))
            directionRad += turnAmount

            // Smooth visual direction update
            visualDirectionRad += (directionRad - visualDirectionRad) * TURN_ANIMATION_SPEED

            // Check for drift conditions
            isDrifting = speed > MAX_SPEED * 0.5f * speedModifier && abs(turnDirection) > 0.5f
        } else {
            isDrifting = false
            // When not turning, align visual direction with actual direction
            visualDirectionRad += (directionRad - visualDirectionRad) * TURN_ANIMATION_SPEED
        }

        // Update position
        updatePosition(deltaTime)
    }

    private fun updatePosition(deltaTime: Float) {
        if (speed == 0f) return

        val moveDistance = speed * deltaTime
        // Более плавное изменение визуального направления
        visualDirectionRad += (directionRad - visualDirectionRad) *
                min(TURN_ANIMATION_SPEED * (1 + speed / MAX_SPEED), 0.1f)

        position = Offset(
            x = position.x + moveDistance * cos(visualDirectionRad),
            y = position.y + moveDistance * sin(visualDirectionRad)
        )
    }

    fun crash(otherCar: Car? = null) {
        if (!isAlive) return

        otherCar?.let {
            if (speed > it.speed) {
                it.crash()
                return
            }
        }

        isAlive = false
        speed = 0f
    }

    // Getters
    fun getSpeed(): Float = speed
    fun getDirection(): Float = directionRad
    fun getVisualDirection(): Float = visualDirectionRad
    fun isAlive(): Boolean = isAlive
    fun isDrifting(): Boolean = isDrifting
}