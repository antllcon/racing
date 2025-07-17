package com.mobility.race.presentation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import com.mobility.race.data.PlayerStateDto
import com.mobility.race.domain.Car
import com.mobility.race.domain.Camera
import com.mobility.race.domain.Map

data class GameState(
    val players: List<Car> = emptyList(),
    val localPlayerId: String = ""
)

data class PlayerInput(
    val isAccelerating: Boolean = false,
    val turnDirection: Float = 0f
)

class GameEngine(
    private var localPlayerId: String,
    private val map: Map,
    private val camera: Camera
) {
    private var state = GameState(localPlayerId = localPlayerId)
    private val remoteCarTargets = mutableMapOf<String, Offset>()
    private val interpolation = 0.15f

    // Вызываем 20 раз в секунд
    fun update(deltaTime: Float, playerInput: PlayerInput): GameState {

        // Применяем текущий ввод
        val localPlayer = state.players.find { it.id == localPlayerId }
        localPlayer?.applyInput(playerInput, deltaTime)

        // Обновляем сущности
        println("GameEngine: Car position before update: ${state.players.find { it.id == localPlayerId }?.position}")
        interpolationCarsUpdate(deltaTime) // Здесь обновляются позиции машин
        camera.update(deltaTime) // Здесь обновляется позиция камеры
        println("GameEngine: Car position after update: ${state.players.find { it.id == localPlayerId }?.position}")
        println("GameEngine: Camera position: ${camera.currentPosition}")

        return this.state
    }

    // Обновление информации с сервера
    fun applyServerState(serverPlayerStates: List<PlayerStateDto>) {
        val serverPlayerIds = extractServerPlayerIds(serverPlayerStates)
        val updatedPlayers = updatePlayersFromServer(serverPlayerStates)

        val finalPlayers = mergePlayers(updatedPlayers, serverPlayerIds)
        this.state = state.copy(players = finalPlayers)

        removeDisconnectedPlayers(serverPlayerIds)
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

    private fun Car.applyInput(input: PlayerInput, deltaTime: Float) {
        if (input.isAccelerating) {
            this.accelerate(deltaTime)
        } else {
            this.decelerate(deltaTime)
        }
        this.startTurn(input.turnDirection)
    }

    private fun interpolationCarsUpdate(deltaTime: Float) {
        state.players.forEach { car ->
            val cellX = car.position.x.toInt().coerceIn(0, map.size - 1)
            val cellY = car.position.y.toInt().coerceIn(0, map.size - 1)

            // Чужие машины - плавно двигаем их к целевой позиции от сервера (ИНТЕРПОЛЯЦИЯ)
            if (car.id != localPlayerId) {
                val targetPosition = remoteCarTargets[car.id] ?: car.position
                car.position = lerp(car.position, targetPosition, interpolation)
            }

            // Своя машина - плавно обновляем перемещение
            car.setSpeedModifier(map.getSpeedModifier(cellX, cellY))
            car.update(deltaTime)
        }
    }

    private fun extractServerPlayerIds(serverPlayers: List<PlayerStateDto>): Set<String> {
        return serverPlayers.map { it.id }.toSet()
    }

    private fun updatePlayersFromServer(serverPlayers: List<PlayerStateDto>): List<Car> {
        val updatedPlayers = mutableListOf<Car>()

        serverPlayers.forEach { serverPlayer ->
            val existingPlayer = findPlayerById(serverPlayer.id)

            if (existingPlayer != null) {
                updateExistingPlayer(existingPlayer, serverPlayer)
                updatedPlayers.add(existingPlayer)
            } else {
                val newPlayer = createNewPlayer(serverPlayer)
                updatedPlayers.add(newPlayer)
                remoteCarTargets[serverPlayer.id] = newPlayer.position
            }
        }

        return updatedPlayers
    }

    private fun findPlayerById(playerId: String): Car? {
        return state.players.find { it.id == playerId }
    }

    private fun updateExistingPlayer(player: Car, serverData: PlayerStateDto) {
        correctPlayers(player, serverData)
    }

    private fun createNewPlayer(serverData: PlayerStateDto): Car {
        return Car(
            id = serverData.id,
            playerName = "Player ${serverData.id.take(2)}",
            isPlayer = false,
            isMultiplayer = true,
            initialPosition = Offset(serverData.posX + 50f, serverData.posY + 50f)
        )
    }

    private fun mergePlayers(updatedPlayers: List<Car>, serverPlayerIds: Set<String>): List<Car> {
        val oldPlayersToKeep = state.players.filter {
                car -> serverPlayerIds.contains(car.id) || car.id == localPlayerId
        }
        return (updatedPlayers + oldPlayersToKeep).distinctBy { it.id }
    }

    private fun removeDisconnectedPlayers(serverPlayerIds: Set<String>) {
        val currentPlayers = state.players
        val disconnectedPlayers = currentPlayers.filterNot { serverPlayerIds.contains(it.id) }
        if (disconnectedPlayers.isNotEmpty()) {
            state = state.copy(players = currentPlayers - disconnectedPlayers.toSet())
            disconnectedPlayers.forEach { remoteCarTargets.remove(it.id) }
        }
    }

    private  fun correctPlayers(clientPlayer: Car, serverPlayer:  PlayerStateDto) {
        if (clientPlayer.id == localPlayerId) {
            correctClientPlayerPos(clientPlayer, serverPlayer, 0.5f)
        } else {
            correctRemouteCarTargets(clientPlayer, serverPlayer)
        }
    }

    private fun correctClientPlayerPos(
        clientPlayer: Car,
        serverPlayer: PlayerStateDto,
        correction: Float
    ) {
        val serverPosition = Offset(serverPlayer.posX, serverPlayer.posY)
        val distance = (serverPosition - clientPlayer.position).getDistance()
        if (distance > correction) {
            clientPlayer.position = serverPosition
        }
        clientPlayer.visualDirection = serverPlayer.direction
    }

    private fun correctRemouteCarTargets(clientPlayer: Car, serverPlayer: PlayerStateDto) {
        val serverPosition = Offset(serverPlayer.posX, serverPlayer.posY)
        remoteCarTargets[serverPlayer.id] = serverPosition
        clientPlayer.visualDirection = serverPlayer.direction
    }
}