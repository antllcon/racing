package com.mobility.race.domain

import androidx.compose.ui.geometry.Offset
import kotlin.math.*
import kotlin.math.PI

// TODO: исправить поле ID (для мультиплеера на Int (много где...))
class Car(
    val playerName: String = "Player",
    var isPlayer: Boolean = true,
    var isMultiplayer: Boolean = false,
    var id: String = "1",
    initialPosition: Offset = Offset.Zero
) {
    companion object {
        // Физика движения
        const val MIN_SPEED = 0f
        const val MAX_SPEED = 3f
        const val ACCELERATION = 0.5f
        const val DECELERATION = 0.8f
        const val BASE_TURN_RATE = 1.2f
        const val DRIFT_TURN_RATE = 2.5f
        const val DRIFT_SPEED_THRESHOLD = 1.8f
        const val SIZE = 0.2f
        const val MAP_SIZE = 10f // Размер карты из GameMap

        // Анимация
        const val VISUAL_LAG_SPEED = 0.05f
        const val DRIFT_ANGLE_OFFSET = 0.2f
    }

    var position: Offset = initialPosition
        private set

    private var _speed: Float = 0f
    private var _direction: Float = 0f
    private var _visualDirection: Float = 0f
    private var _turnInput: Float = 0f
    private var _isDrifting: Boolean = false
    private var _speedModifier: Float = 1f

    val speed: Float get() = _speed
    val direction: Float get() = _direction
    val visualDirection: Float get() = _visualDirection
    val isDrifting: Boolean get() = _isDrifting

    fun setSpeedModifier(modifier: Float) {
        _speedModifier = modifier.coerceIn(0f, 1f)
    }

    fun update(deltaTime: Float) {
        updateDriftState()
        updateTurning(deltaTime)
        updatePosition(deltaTime)
        updateVisualDirection(deltaTime)
    }

    private fun updateDriftState() {
        _isDrifting = _speed > DRIFT_SPEED_THRESHOLD * _speedModifier &&
                abs(_turnInput) > 0.7f
    }

    private fun updateTurning(deltaTime: Float) {
        if (_speed == 0f || _turnInput == 0f) return

        val turnRate = if (_isDrifting) DRIFT_TURN_RATE else BASE_TURN_RATE
        val turnAmount = _turnInput * turnRate * deltaTime * (_speed / MAX_SPEED)
        _direction += turnAmount
    }

    private fun updatePosition(deltaTime: Float) {
        if (_speed == 0f) return

        val moveDistance = _speed * deltaTime
        val newPosition = Offset(
            x = position.x + moveDistance * cos(_direction),
            y = position.y + moveDistance * sin(_direction)
        )

        // Ограничиваем позицию в пределах карты с учетом размера машины
        position = Offset(
            x = newPosition.x.coerceIn(0f, MAP_SIZE - SIZE),
            y = newPosition.y.coerceIn(0f, MAP_SIZE - SIZE)
        )
    }

    private fun updateVisualDirection(deltaTime: Float) {
        val targetDirection = if (_isDrifting) {
            _direction + (DRIFT_ANGLE_OFFSET * _turnInput)
        } else {
            _direction
        }

        _visualDirection += (targetDirection - _visualDirection) *
                VISUAL_LAG_SPEED * (1 + _speed / MAX_SPEED)
    }

    fun accelerate(deltaTime: Float) {
        _speed = min(_speed + ACCELERATION * deltaTime * _speedModifier,
            MAX_SPEED * _speedModifier)
    }

    fun decelerate(deltaTime: Float) {
        _speed = max(_speed - DECELERATION * deltaTime * _speedModifier, MIN_SPEED)
    }

    fun startTurn(direction: Float) {
        // Если касание сзади (определяется в обработчике касаний), поворот не происходит
        _turnInput = direction.coerceIn(-1f, 1f)
    }

    fun stopTurn() {
        _turnInput = 0f
    }

    fun reset(position: Offset = Offset(5f, 5f)) {
        this.position = position
        _speed = 0f
        _direction = 0f
        _visualDirection = 0f
        _turnInput = 0f
        _isDrifting = false
        _speedModifier = 1f
    }
}