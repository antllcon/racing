package com.mobility.race.domain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import com.mobility.race.data.PlayerStateDto
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

class Car(
    val playerName: String = "Player",
    var isPlayer: Boolean = true,
    var isMultiplayer: Boolean = false,
    var id: String = "1",
    initialPosition: Offset = Offset.Zero
) {
    companion object {
        const val MIN_SPEED = 0f
        const val MAX_SPEED = 0.2f
        const val ACCELERATION = 0.02f
        const val DECELERATION = 0.1f
        const val BASE_TURN_RATE = 1.2f
        const val DRIFT_TURN_RATE = 2.5f
        const val DRIFT_SPEED_THRESHOLD = 1.8f
        const val WIDTH = 0.035f
        const val LENGTH = 0.06f
        const val MAP_SIZE = 10f

        const val VISUAL_LAG_SPEED = 0.7f
        const val DRIFT_ANGLE_OFFSET = 0.2f
    }

    // ✅ Ключевое изменение: используем mutableStateOf с делегатом
    var position by mutableStateOf(initialPosition)
    var corners: List<Offset> = emptyList()
        private set

    private var _speed: Float = 0f
    private var _direction: Float = 0f
    private var _turnInput: Float = 0f
    private var _isDrifting: Boolean = false
    private var _speedModifier: Float = 1f
    private var _targetSpeed: Float = 0f
    private var _targetTurnInput: Float = 0f

    val speed: Float get() = _speed
    var direction: Float = 0.0f
    var visualDirection: Float = 0f
    val isDrifting: Boolean get() = _isDrifting
    val mass: Float = 1f
    val momentOfInertia: Float = (1f / 12f) * mass * (WIDTH * WIDTH + LENGTH * LENGTH)
    var angularVelocity: Float = 0f

    // ✅ Ручная реализация copy, которая правильно работает с MutableState
    fun copy(
        playerName: String = this.playerName,
        isPlayer: Boolean = this.isPlayer,
        isMultiplayer: Boolean = this.isMultiplayer,
        id: String = this.id,
        initialPosition: Offset = this.position // Берем текущую позицию для новой машины
    ): Car {
        val newCar = Car(playerName, isPlayer, isMultiplayer, id, initialPosition)
        // Копируем внутреннее состояние
        newCar._speed = this._speed
        newCar._direction = this._direction
        newCar._turnInput = this._turnInput
        newCar._isDrifting = this._isDrifting
        newCar._speedModifier = this._speedModifier
        newCar._targetSpeed = this._targetSpeed
        newCar._targetTurnInput = this._targetTurnInput
        newCar.direction = this.direction
        newCar.visualDirection = this.visualDirection
        newCar.angularVelocity = this.angularVelocity
        return newCar
    }

    fun setSpeedModifier(modifier: Float) {
        _speedModifier = modifier.coerceIn(0f, 1f)
    }

    fun update(deltaTime: Float) {
        val safeDeltaTime = deltaTime.coerceIn(0.001f, 0.016f)

        _direction += angularVelocity * safeDeltaTime
        angularVelocity *= 0.98f

        updateTurnInput(safeDeltaTime)
        updateDriftState()
        updateTurning(safeDeltaTime)
        updatePosition(safeDeltaTime)
        updateVisualDirection(safeDeltaTime)
        updateCorners()
    }


    private fun updateCorners() {
        val halfLength = LENGTH / 2
        val halfWidth = WIDTH / 2

        val frontLeft = Offset(-halfLength, -halfWidth)
        val frontRight = Offset(-halfLength, halfWidth)
        val rearLeft = Offset(halfLength, -halfWidth)
        val rearRight = Offset(halfLength, halfWidth)

        val rotatedCorners = listOf(frontLeft, frontRight, rearLeft, rearRight).map { corner ->
            rotatePoint(corner, _direction)
        }

        // При обращении к position.x и position.y .value не нужно благодаря делегату
        corners = rotatedCorners.map { corner ->
            Offset(position.x + corner.x, position.y + corner.y)
        }
    }

    private fun rotatePoint(point: Offset, angle: Float): Offset {
        val cosA = cos(angle)
        val sinA = sin(angle)
        return Offset(
            x = point.x * cosA - point.y * sinA,
            y = point.x * sinA + point.y * cosA
        )
    }

    fun checkCollision(other: Car): CollisionResult {
        if (this.corners.isEmpty() || other.corners.isEmpty()) {
            return CollisionResult(isColliding = false)
        }
        if (!getBoundingBox().overlaps(other.getBoundingBox())) {
            return CollisionResult(isColliding = false)
        }
        // Замените detectCollision на вашу реализацию
        return CollisionResult(isColliding = false) // Placeholder
    }

    fun setSpeedAndDirectionFromVelocity(velocity: Offset) {
        this._speed = velocity.getDistance()
        if (this._speed > 0.001f) {
            this._direction = atan2(velocity.y, velocity.x)
        }
    }

    private fun getBoundingBox(): Rect {
        if (corners.isEmpty()) return Rect(position, Size.Zero)

        val minX = corners.minOf { it.x }
        val minY = corners.minOf { it.y }
        val maxX = corners.maxOf { it.x }
        val maxY = corners.maxOf { it.y }

        return Rect(minX, minY, maxX, maxY)
    }

    private fun updateTurnInput(deltaTime: Float) {
        _turnInput = lerp(_turnInput, _targetTurnInput, 0.01f, deltaTime)
    }

    private fun updateDriftState() {
        _isDrifting = _speed > DRIFT_SPEED_THRESHOLD * _speedModifier &&
                abs(_turnInput) > 0.5f
    }

    private fun updateTurning(deltaTime: Float) {
        if (_speed == 0f || _turnInput == 0f) return

        val turnRate = if (_isDrifting) DRIFT_TURN_RATE else BASE_TURN_RATE
        val turnAmount = _turnInput * turnRate * deltaTime * sqrt(_speed / MAX_SPEED)
        _direction += turnAmount
    }

    private fun updatePosition(deltaTime: Float) {
        if (_speed == 0f) return

        val moveDistance = _speed * deltaTime
        val maxMove = MAP_SIZE
        val actualMove = moveDistance.coerceIn(-maxMove, maxMove)

        val newPosition = Offset(
            x = (position.x + actualMove * cos(_direction)).coerceIn(WIDTH, MAP_SIZE - WIDTH),
            y = (position.y + actualMove * sin(_direction)).coerceIn(WIDTH, MAP_SIZE - WIDTH)
        )
        // ✅ Это присваивание теперь вызовет рекомпозицию
        position = newPosition
    }

    private fun updateVisualDirection(deltaTime: Float) {
        val targetDirection = if (_isDrifting) {
            _direction + (DRIFT_ANGLE_OFFSET * _turnInput)
        } else {
            _direction
        }

        val lagFactor = VISUAL_LAG_SPEED * deltaTime * 60f
        visualDirection =
            lerp(visualDirection, targetDirection, lagFactor.coerceIn(0.01f, 1f), deltaTime)
    }

    fun accelerate(deltaTime: Float) {
        _targetSpeed = min(
            MAX_SPEED * _speedModifier,
            _targetSpeed + ACCELERATION * deltaTime * _speedModifier
        )
        _speed = lerp(_speed, _targetSpeed, 0.001f, deltaTime)
    }

    fun decelerate(deltaTime: Float) {
        _targetSpeed = max(MIN_SPEED, _targetSpeed - DECELERATION * deltaTime * _speedModifier)
        _speed = lerp(_speed, _targetSpeed, 0.001f, deltaTime)
    }

    private fun lerp(start: Float, end: Float, factor: Float, deltaTime: Float): Float {
        return start + (end - start) * factor * deltaTime * 60f
    }

    fun startTurn(direction: Float) {
        _targetTurnInput = direction.coerceIn(-1f, 1f)
    }

    fun stopTurn() {
        _targetTurnInput = 0f
    }

    fun reset(newPosition: Offset = Offset(5f, 5f)) {
        this.position = newPosition
        _speed = 0f
        _direction = 0f
        visualDirection = 0f
        _turnInput = 0f
        _isDrifting = false
        _speedModifier = 1f
        updateCorners()
    }
}
// Placeholder, чтобы код компилировался. У вас должна быть своя реализация.

// TODO: сделать
//data class CollisionResult(val isColliding: Boolean)
//fun detectCollision(car1: Car, car2: Car): CollisionResult {
//    return CollisionResult(isColliding = false)
//}

fun Car.drawCar(
    camera: GameCamera,
    drawScope: DrawScope,
    isLocalPlayer: Boolean,
    scaledCellSize: Float
) {
    with(drawScope) {
        val carScreenPos = camera.worldToScreen(position)
        val carColor = if (isLocalPlayer) Color.Red else Color.Green

        rotate(
            degrees = visualDirection * (180f / PI.toFloat()),
            pivot = carScreenPos
        ) {
            val carWidthPx = Car.WIDTH * scaledCellSize
            val carLengthPx = Car.LENGTH * scaledCellSize
            drawRect(
                color = carColor,
                topLeft = Offset(carScreenPos.x - carLengthPx / 2, carScreenPos.y - carWidthPx / 2),
                size = Size(carLengthPx, carWidthPx)
            )
        }
    }
}

fun Car.toPlayerStateDto(): PlayerStateDto {
    return PlayerStateDto(
        id = this.id,
        posX = this.position.x,
        posY = this.position.y,
        direction = this.direction
    )
}