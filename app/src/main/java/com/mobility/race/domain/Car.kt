package com.mobility.race.domain

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import kotlin.math.*

class Car(
    val playerName: String = "Player",
    var isPlayer: Boolean = true,
    var isMultiplayer: Boolean = false,
    var id: String = "1",
    initialPosition: Offset = Offset.Zero
) {
    var position = mutableStateOf<Offset>(initialPosition)
    private var corners: List<Offset> = emptyList()
        private set

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
    val visualDirection: Float get() = _visualDirection
    val isDrifting: Boolean get() = _isDrifting

    fun update(elapsedTime: Float, directionAngle: Float?, gameMap: GameMap) {
        updatePosition(elapsedTime)

        if (directionAngle == null) {
            decelerate()
        } else {
            accelerate()
        }

        setSpeedModifier(gameMap)
        handleAnglesDiff(elapsedTime, directionAngle)
    }

    private fun handleAnglesDiff(elapsedTime: Float, newAngle: Float?) {
        updateDriftState()

        if (newAngle != null) {
            _direction = newAngle

            updateTurning(elapsedTime)
            updateVisualDirection(elapsedTime)
        }
    }

    private fun setSpeedModifier(gameMap: GameMap) {
        val cellX = position.value.x.toInt().coerceIn(0, gameMap.size - 1)
        val cellY = position.value.y.toInt().coerceIn(0, gameMap.size - 1)

        _speedModifier = gameMap.getSpeedModifier(cellX, cellY).coerceIn(0f, 1f)
    }

    private fun updatePosition(deltaTime: Float) {
        val moveDistance = _speed * deltaTime * _speedModifier
        val maxMove = MAP_SIZE * 0.5f
        val actualMove = moveDistance.coerceIn(-maxMove, maxMove)

        val newPosition = Offset(
            x = (position.value.x + actualMove * cos(_direction)),
            y = (position.value.y + actualMove * sin(_direction))
        )

        position.value = newPosition
    }

    private fun decelerate() {
        if (_speed > MIN_SPEED) {
            _speed -= DECELERATION
        }
        if (_speed < MIN_SPEED) {
            _speed = MIN_SPEED
        }
    }

    private fun accelerate() {
        if (_speed < MAX_SPEED) {
            _speed += ACCELERATION
        }
        if (_speed > MAX_SPEED) {
            _speed = MAX_SPEED
        }
    }

    private fun updateDriftState() {
        _isDrifting = _speed > DRIFT_SPEED_THRESHOLD * _speedModifier &&
                abs(_turnInput) > 0.7f
    }

    private fun updateTurning(elapsedTime: Float) {
        val turnRate = if (_isDrifting) DRIFT_TURN_RATE else BASE_TURN_RATE
        val turnAmount = _turnInput * turnRate * elapsedTime * (_speed / MAX_SPEED)
        _direction += turnAmount
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

//    fun update(elapsedTime: Float) {
//        val safeDeltaTime = elapsedTime.coerceIn(0.001f, 0.1f)
//        updateTurnInput(safeDeltaTime)
//        updateDriftState()
//        updateTurning(safeDeltaTime)
//        updatePosition(safeDeltaTime)
//        updateVisualDirection(safeDeltaTime)
//        updateCorners()
//    }
//
//    private fun updateCorners() {
//        val halfLength = LENGTH / 2
//        val halfWidth = WIDTH / 2
//
//        val frontLeft = Offset(-halfLength, -halfWidth)
//        val frontRight = Offset(-halfLength, halfWidth)
//        val rearLeft = Offset(halfLength, -halfWidth)
//        val rearRight = Offset(halfLength, halfWidth)
//
//        val rotatedCorners = listOf(frontLeft, frontRight, rearLeft, rearRight).map { corner ->
//            rotatePoint(corner, _direction)
//        }
//
//        corners = rotatedCorners.map { corner ->
//            Offset(position.value.x + corner.x, position.value.y + corner.y)
//        }
//    }
//
//    private fun rotatePoint(point: Offset, angle: Float): Offset {
//        val cosA = cos(angle)
//        val sinA = sin(angle)
//        return Offset(
//            x = point.x * cosA - point.y * sinA,
//            y = point.x * sinA + point.y * cosA
//        )
//    }
//
//    fun checkCollision(other: Car): Boolean {
//        if (this.corners.isEmpty() || other.corners.isEmpty()) return false
//
//        if (!getBoundingBox().overlaps(other.getBoundingBox())) {
//            return false
//        }
//
//        return satCollisionCheck(this, other)
//    }
//
//    private fun getBoundingBox(): Rect {
//        if (corners.isEmpty()) return Rect(position.value, Size.Zero)
//
//        val minX = corners.minOf { it.x }
//        val minY = corners.minOf { it.y }
//        val maxX = corners.maxOf { it.x }
//        val maxY = corners.maxOf { it.y }
//
//        return Rect(minX, minY, maxX, maxY)
//    }
//
//    private fun satCollisionCheck(car1: Car, car2: Car): Boolean {
//        val axes = mutableListOf<Offset>()
//
//        for (i in 0 until car1.corners.size) {
//            val p1 = car1.corners[i]
//            val p2 = car1.corners[(i + 1) % car1.corners.size]
//            val edge = p1 - p2
//            axes.add(Offset(edge.y, -edge.x).normalized())
//        }
//
//        for (i in 0 until car2.corners.size) {
//            val p1 = car2.corners[i]
//            val p2 = car2.corners[(i + 1) % car2.corners.size]
//            val edge = p1 - p2
//            axes.add(Offset(edge.y, -edge.x).normalized())
//        }
//
//        for (axis in axes) {
//            if (!overlapOnAxis(car1, car2, axis)) {
//                return false
//            }
//        }
//
//        return true
//    }
//
//    private fun overlapOnAxis(car1: Car, car2: Car, axis: Offset): Boolean {
//        val proj1 = projectCorners(car1.corners, axis)
//        val proj2 = projectCorners(car2.corners, axis)
//
//        return proj1.first <= proj2.second && proj2.first <= proj1.second
//    }
//
//    private fun projectCorners(corners: List<Offset>, axis: Offset): Pair<Float, Float> {
//        var min = Float.POSITIVE_INFINITY
//        var max = Float.NEGATIVE_INFINITY
//
//        for (corner in corners) {
//            val dot = corner.x * axis.x + corner.y * axis.y
//            min = minOf(min, dot)
//            max = maxOf(max, dot)
//        }
//
//        return Pair(min, max)
//    }
//
//    private fun Offset.normalized(): Offset {
//        val length = sqrt(x * x + y * y)
//        return if (length > 0) Offset(x / length, y / length) else this
//    }
//
//    private fun updateTurnInput(deltaTime: Float) {
//        _turnInput = lerp(_turnInput, _targetTurnInput, 0.2f, deltaTime)
//    }
//
//
//    private fun updateTurning(deltaTime: Float) {
//        if (_speed == 0f || _turnInput == 0f) return
//
//        val turnRate = if (_isDrifting) DRIFT_TURN_RATE else BASE_TURN_RATE
//        val turnAmount = _turnInput * turnRate * deltaTime * sqrt(_speed / MAX_SPEED)
//        _direction += turnAmount
//    }
//
//
//    private fun updateVisualDirection(deltaTime: Float) {
//        val targetDirection = if (_isDrifting) {
//            _direction + (DRIFT_ANGLE_OFFSET * _turnInput)
//        } else {
//            _direction
//        }
//
//        val lagFactor = VISUAL_LAG_SPEED * deltaTime * 60f
//        _visualDirection = lerp(_visualDirection, targetDirection, lagFactor.coerceIn(0f, 0.5f), deltaTime)
//    }
//
//    fun accelerate(deltaTime: Float) {
//        _targetSpeed = min(MAX_SPEED * _speedModifier, _targetSpeed + ACCELERATION * deltaTime * _speedModifier)
//        _speed = lerp(_speed, _targetSpeed, 0.1f, deltaTime)
//    }
//
//    fun decelerate(deltaTime: Float) {
//        _targetSpeed = max(MIN_SPEED, _targetSpeed - DECELERATION * deltaTime * _speedModifier)
//        _speed = lerp(_speed, _targetSpeed, 0.1f, deltaTime)
//    }
//
//    private fun lerp(start: Float, end: Float, factor: Float, deltaTime: Float): Float {
//        return start + (end - start) * factor * deltaTime * 60f
//    }
//
//    fun startTurn(direction: Float) {
//        _targetTurnInput = direction.coerceIn(-1f, 1f)
//    }
//
//    fun stopTurn() {
//        _targetTurnInput = 0f
//    }
//
//    fun reset(position: Offset = Offset(5f, 5f)) {
//        this.position.value = position
//        _speed = 0f
//        _direction = 0f
//        _visualDirection = 0f
//        _turnInput = 0f
//        _isDrifting = false
//        _speedModifier = 1f
//        updateCorners()
//    }

    companion object {
        const val MIN_SPEED = 0f
        const val MAX_SPEED = 0.5f
        const val ACCELERATION = 0.002f
        const val DECELERATION = 0.001f
        const val BASE_TURN_RATE = 1.2f
        const val DRIFT_TURN_RATE = 2.5f
        const val DRIFT_SPEED_THRESHOLD = 1.8f
        const val WIDTH = 0.035f
        const val LENGTH = 0.06f
        const val MAP_SIZE = 10f

        const val VISUAL_LAG_SPEED = 0.05f
        const val DRIFT_ANGLE_OFFSET = 0.2f
    }
}