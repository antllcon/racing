package com.mobility.race.domain

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

// TODO: исправить поле ID (для мультиплеера на Int (много где...))
data class Car(
    val playerName: String = "Player",
    var isPlayer: Boolean = true,
    var isMultiplayer: Boolean = false,
    var id: String = "1",
    val initialPosition: Offset = Offset.Zero
) {
    companion object {
        const val MIN_SPEED = 0f
        const val MAX_SPEED = 0.2f
        const val ACCELERATION = 0.60f
        const val DECELERATION = 0.1f
        const val BASE_TURN_RATE = 1.2f
        const val DRIFT_TURN_RATE = 2.5f
        const val DRIFT_SPEED_THRESHOLD = 1.8f
        const val WIDTH = 0.035f
        const val LENGTH = 0.06f
        const val MAP_SIZE = 10f

        const val VISUAL_LAG_SPEED = 0.05f
        const val DRIFT_ANGLE_OFFSET = 0.2f

    }

    var position: Offset = initialPosition

    private var _speed: Float = 0f
    private var _direction: Float = 0f
    private var _visualDirection: Float = 0f
    private var _turnInput: Float = 0f
    private var _isDrifting: Boolean = false
    private var _speedModifier: Float = 1f
    private var _targetSpeed: Float = 0f
    private var _targetTurnInput: Float = 0f


    val speed: Float get() = _speed
    val direction: Float get() = _direction
    var visualDirection: Float = 0.0f

    val isDrifting: Boolean get() = _isDrifting

    fun setSpeedModifier(modifier: Float) {
        _speedModifier = modifier.coerceIn(0f, 1f)
    }

    fun update(deltaTime: Float) {
        val safeDeltaTime = deltaTime.coerceIn(0.001f, 0.1f) // Ограничиваем слишком большие и малые значения
        updateTurnInput(safeDeltaTime)
        updateDriftState()
        updateTurning(safeDeltaTime)
        updatePosition(safeDeltaTime)
        updateVisualDirection(safeDeltaTime)
    }

    fun drawCar(
        camera: Camera,
//        baseCellSize: Float,
//        zoom: Float,
        drawScope: DrawScope,
        isLocalPlayer: Boolean = false
    ) {
        val screenPosition = camera.worldToScreen(position)
//        val scaledCellSize = baseCellSize * zoom
//        val carScreenPos = camera.worldToScreen(position)
        val carColor = if (isLocalPlayer) Color.Blue else Color.Red

        drawScope.rotate(
            degrees = visualDirection * (180f / PI.toFloat()),
            pivot = screenPosition
        ) {
            val carWidthPx = WIDTH
            val carLengthPx = LENGTH

            drawScope.drawRect(
                color = carColor,
                topLeft = Offset(screenPosition.x - carLengthPx / 2, screenPosition.y - carWidthPx / 2),
                size = Size(carLengthPx, carWidthPx)
            )
        }
    }

    fun accelerate(deltaTime: Float) {
        _targetSpeed = min(MAX_SPEED * _speedModifier, _targetSpeed + ACCELERATION * deltaTime * _speedModifier)
        _speed = lerp(_speed, _targetSpeed, 0.1f * deltaTime * 60f)
    }

    fun decelerate(deltaTime: Float) {
        _targetSpeed = max(MIN_SPEED, _targetSpeed - DECELERATION * deltaTime * _speedModifier)
        _speed = lerp(_speed, _targetSpeed, 0.1f * deltaTime * 60f)
    }

    private fun lerp(start: Float, end: Float, amount: Float): Float {
        return start + (end - start) * amount.coerceIn(0f, 1f)
    }

    fun startTurn(direction: Float) {
        _targetTurnInput = direction.coerceIn(-1f, 1f)
    }

    fun stopTurn() {
        _targetTurnInput = 0f
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

    private fun updateTurnInput(deltaTime: Float) {
        _turnInput = lerp(_speed, _targetSpeed, 0.1f * deltaTime * 60f)
    }
    private fun updateDriftState() {
        _isDrifting = _speed > DRIFT_SPEED_THRESHOLD * _speedModifier &&
                abs(_turnInput) > 0.7f
    }

    private fun updateTurning(deltaTime: Float) {
        if (_speed == 0f || _turnInput == 0f) return

        val turnRate = if (_isDrifting) DRIFT_TURN_RATE else BASE_TURN_RATE
        // Добавляем зависимость от deltaTime и плавность поворота
        val turnAmount = _turnInput * turnRate * deltaTime * sqrt(_speed / MAX_SPEED)
        _direction += turnAmount
    }

    private fun updatePosition(deltaTime: Float) {
        if (_speed == 0f) return

        val moveDistance = _speed * deltaTime
        val maxMove = MAP_SIZE * 0.5f // Ограничение максимального перемещения
        val actualMove = moveDistance.coerceIn(-maxMove, maxMove)

        val newPosition = Offset(
            x = (position.x + actualMove * cos(_direction)).coerceIn(WIDTH, MAP_SIZE - WIDTH),
            y = (position.y + actualMove * sin(_direction)).coerceIn(WIDTH, MAP_SIZE - WIDTH)
        )

        position = newPosition
    }

    private fun updateVisualDirection(deltaTime: Float) {
        if (_isDrifting) {
            _direction + (DRIFT_ANGLE_OFFSET * _turnInput)
        } else {
            _direction
        }

        // Более стабильный расчет с учетом deltaTime
        VISUAL_LAG_SPEED * deltaTime * 60f
        _visualDirection = lerp(_speed, _targetSpeed, 0.1f * deltaTime * 60f)
    }
}