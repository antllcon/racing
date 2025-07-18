package com.mobility.race.presentation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import com.mobility.race.data.PlayerStateDto
import com.mobility.race.domain.Car
import com.mobility.race.domain.GameCamera
import com.mobility.race.domain.GameMap

data class GameState(
    val players: List<Car> = emptyList(),
    val localPlayerId: String = ""
)

class GameEngine(
    private var localPlayerId: String,
    private val map: GameMap,
    private val camera: GameCamera
) {
    private var state = GameState(localPlayerId = localPlayerId)
    private val remoteCarTargets = mutableMapOf<String, Offset>()
    private val interpolation = 0.15f // Фактор интерполяции для удаленных машин

    fun update(deltaTime: Float, playerInput: PlayerInput): GameState {
        val currentPlayersMap = state.players.associateBy { it.id }.toMutableMap()

        // Обновление локального игрока
        currentPlayersMap[localPlayerId]?.let { localPlayer ->
            if (playerInput.isAccelerating) {
                localPlayer.accelerate(deltaTime)
            } else {
                localPlayer.decelerate(deltaTime)
            }

            if (playerInput.turnDirection != 0f) {
                localPlayer.startTurn(playerInput.turnDirection)
            } else {
                localPlayer.stopTurn()
            }

            val cellX = localPlayer.position.x.toInt().coerceIn(0, map.size - 1)
            val cellY = localPlayer.position.y.toInt().coerceIn(0, map.size - 1)
            localPlayer.setSpeedModifier(map.getSpeedModifier(cellX, cellY))

            localPlayer.update(deltaTime)
        }

        // Обновление чужих машин с интерполяцией
        currentPlayersMap.values.forEach { car ->
            if (car.id != localPlayerId) {
                val targetPosition = remoteCarTargets[car.id]
                if (targetPosition != null) {
                    car.position = lerp(car.position, targetPosition, interpolation)
                }
                // Чужие машины также должны обновлять свою внутреннюю логику (скорость, направление и т.д.)
                // исходя из серверных данных или собственной симуляции, если это необходимо.
                // В данном случае, мы обновляем их position и visualDirection из serverPlayerStates,
                // а затем Car.update() для плавности.
                val cellX = car.position.x.toInt().coerceIn(0, map.size - 1)
                val cellY = car.position.y.toInt().coerceIn(0, map.size - 1)
                car.setSpeedModifier(map.getSpeedModifier(cellX, cellY))
                car.update(deltaTime)
            }
        }

        camera.update(deltaTime)
        this.state = state.copy(players = currentPlayersMap.values.toList())
        return this.state
    }

    fun applyServerState(serverPlayerStates: List<PlayerStateDto>) {
        val serverPlayerIds = serverPlayerStates.map { it.id }.toSet()
        val currentPlayersById = state.players.associateBy { it.id }
        val newPlayers = mutableListOf<Car>()

        serverPlayerStates.forEach { serverPlayerDto ->
            val existingPlayer = currentPlayersById[serverPlayerDto.id]

            if (existingPlayer != null) {
                // Обновляем существующего игрока
                if (existingPlayer.id == localPlayerId) {
                    // Для локального игрока: всегда применяем серверную позицию для коррекции,
                    // так как локальная симуляция может разойтись
                    val serverPosition = Offset(serverPlayerDto.posX, serverPlayerDto.posY)
                    //println("!!!$serverPosition - вот гад!!!") // Теперь это покажет реальные float значения
                    existingPlayer.position = serverPosition
                    existingPlayer.visualDirection = serverPlayerDto.direction
                    existingPlayer.direction = serverPlayerDto.direction // Также синхронизируем _direction
                } else {
                    // Для удаленных игроков: обновляем их Car объект и цель для интерполяции
                    remoteCarTargets[serverPlayerDto.id] = Offset(serverPlayerDto.posX, serverPlayerDto.posY)
                    existingPlayer.visualDirection = serverPlayerDto.direction
                    existingPlayer.direction = serverPlayerDto.direction // Синхронизируем направление для расчетов
                }
                newPlayers.add(existingPlayer)
            } else {
                // Добавляем нового игрока
                val newCar = Car(
                    id = serverPlayerDto.id,
                    playerName = "Player ${serverPlayerDto.id.take(2)}",
                    isPlayer = false,
                    isMultiplayer = true,
                    initialPosition = Offset(serverPlayerDto.posX, serverPlayerDto.posY)
                )
                newPlayers.add(newCar)
                remoteCarTargets[serverPlayerDto.id] = newCar.position
            }
        }

        // Отфильтровываем игроков, которые отключились
        val playersToKeep = newPlayers.filter { serverPlayerIds.contains(it.id) || it.id == localPlayerId }
        val disconnectedPlayers = currentPlayersById.values.filterNot { serverPlayerIds.contains(it.id) || it.id == localPlayerId }

        this.state = state.copy(players = playersToKeep)
        disconnectedPlayers.forEach { remoteCarTargets.remove(it.id) }
    }

    fun updateLocalPlayerId(newId: String) {
        if (this.localPlayerId != newId) {
            this.localPlayerId = newId
            this.state = state.copy(localPlayerId = newId)
        }
    }

    fun getCurrentState(): GameState {
        return this.state
    }
}