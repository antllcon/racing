package com.mobility.race.presentation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import com.mobility.race.data.PlayerStateDto
import com.mobility.race.domain.Car
import com.mobility.race.domain.GameCamera
import com.mobility.race.domain.GameMap
import com.mobility.race.presentation.multiplayer.PlayerInput

data class GameState(
    val players: List<Car> = emptyList(),
    val localPlayerId: String = ""
)

//private fun lerpAngle(start: Float, end: Float, factor: Float): Float {
//    var delta = end - start
//    if (delta > Math.PI) {
//        delta -= 2 * Math.PI.toFloat()
//    } else if (delta < -Math.PI) {
//        delta += 2 * Math.PI.toFloat()
//    }
//    return start + delta * factor
//}
//
//class GameEngine(
//    private var localPlayerId: String,
//    private val map: GameMap,
//    private val camera: GameCamera
//) {
//    private var state = GameState(localPlayerId = localPlayerId)
//    private val remoteCarTargets = mutableMapOf<String, Offset>()
//    private val remoteDirectionTargets = mutableMapOf<String, Float>()
//    private val interpolation = 0.15f
//
//    fun update(deltaTime: Float, playerInput: PlayerInput): GameState {
//        val currentPlayersMap = state.players.associateBy { it.id }.toMutableMap()
//
//        currentPlayersMap[localPlayerId]?.let { localPlayer ->
//            if (playerInput.isAccelerating) {
//                localPlayer.accelerate()
//            } else {
//                localPlayer.decelerate()
//            }
//            if (playerInput.turnDirection != 0f) {
//                localPlayer.startTurn(playerInput.turnDirection)
//            } else {
//                localPlayer.stopTurn()
//            }
//            val cellX = localPlayer.position.x.toInt().coerceIn(0, map.size - 1)
//            val cellY = localPlayer.position.y.toInt().coerceIn(0, map.size - 1)
//            localPlayer.setSpeedModifier(map.getSpeedModifier(cellX, cellY))
//            localPlayer.update(deltaTime)
//        }
//
//        currentPlayersMap.values.forEach { car ->
//            if (car.id != localPlayerId) {
//                remoteCarTargets[car.id]?.let { targetPosition ->
//                    car.position = lerp(car.position, targetPosition, interpolation)
//                }
//                remoteDirectionTargets[car.id]?.let { targetDirection ->
//                    car.visualDirection = lerpAngle(car.visualDirection, targetDirection, interpolation)
//                    car.direction = car.visualDirection
//                }
//            }
//        }
//
//        this.state = state.copy(players = currentPlayersMap.values.toList())
//        return this.state
//    }
//
//    fun applyServerState(serverPlayerStates: List<PlayerStateDto>) {
//        val serverPlayerIds = serverPlayerStates.map { it.id }.toSet()
//        val currentPlayersById = state.players.associateBy { it.id }
//        val newPlayers = mutableListOf<Car>()
//
//        serverPlayerStates.forEach { serverPlayerDto ->
//            val existingPlayer = currentPlayersById[serverPlayerDto.id]
//
//            if (existingPlayer != null) {
//                if (existingPlayer.id == localPlayerId) {
//                    // ✅ Решение проблемы с дерганьем: ПЛАВНАЯ коррекция, а не резкий скачок
//                    val serverPosition = Offset(serverPlayerDto.posX, serverPlayerDto.posY)
//
//                    // Плавно корректируем позицию к серверной (например, на 30% от ошибки за кадр)
//                    existingPlayer.setNewPosition(lerp(existingPlayer.position, serverPosition, 0.3f))
//
//                    // Направление тоже корректируем плавно
//                    existingPlayer.direction = lerpAngle(existingPlayer.direction, serverPlayerDto.direction, 0.3f)
//                    existingPlayer.visualDirection = lerpAngle(existingPlayer.visualDirection, serverPlayerDto.direction, 0.3f)
//                } else {
//                    // Для удаленных игроков: просто сохраняем целевые значения для интерполяции
//                    remoteCarTargets[existingPlayer.id] = Offset(serverPlayerDto.posX, serverPlayerDto.posY)
//                    remoteDirectionTargets[existingPlayer.id] = serverPlayerDto.direction
//                    println("REMOTE_PLAYER_DATA: ID=${existingPlayer.id}, ServerDir=${serverPlayerDto.direction}")
//                }
//                newPlayers.add(existingPlayer)
//            } else {
//                // Добавляем нового игрока с корректным начальным направлением
//                val newCar = Car(
//                    id = serverPlayerDto.id,
//                    playerName = "Player ${serverPlayerDto.id.take(2)}",
//                    isPlayer = false,
//                    isMultiplayer = true,
//                    position = Offset(serverPlayerDto.posX, serverPlayerDto.posY)
//                )
//                // Устанавливаем начальное направление
//                newCar.direction = serverPlayerDto.direction
//                newCar.visualDirection = serverPlayerDto.direction
//
//                newPlayers.add(newCar)
//
//                // И сразу задаем цель для интерполяции
//                remoteCarTargets[newCar.id] = newCar.position
//                remoteDirectionTargets[newCar.id] = newCar.direction
//            }
//        }
//
//        val playersToKeep = newPlayers.filter { serverPlayerIds.contains(it.id) || it.id == localPlayerId }
//        val disconnectedPlayers = currentPlayersById.values.filterNot { serverPlayerIds.contains(it.id) || it.id == localPlayerId }
//
//        this.state = state.copy(players = playersToKeep)
//
//        // Удаляем цели для отключившихся игроков
//        disconnectedPlayers.forEach {
//            remoteCarTargets.remove(it.id)
//            remoteDirectionTargets.remove(it.id)
//        }
//    }
//
//    fun updateLocalPlayerId(newId: String) {
//        if (this.localPlayerId != newId) {
//            this.localPlayerId = newId
//            this.state = state.copy(localPlayerId = newId)
//        }
//    }
//
//    fun getCurrentState(): GameState {
//        return this.state
//    }
//
//    fun updatePlayerInstance(player: Car) {
//        val playerIndex = state.players.indexOfFirst { it.id == "TEMP_ID" && player.id != "TEMP_ID" }
//        if (playerIndex != -1) {
//            val mutablePlayers = state.players.toMutableList()
//            mutablePlayers[playerIndex] = player
//            state = state.copy(players = mutablePlayers)
//            println("GameEngine: Player instance updated for ID ${player.id}")
//        }
//    }
//}