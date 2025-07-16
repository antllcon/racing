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

data class PlayerInput(
    val isAccelerating: Boolean = false,
    val turnDirection: Float = 0f
)

class GameEngine(
    private val gameMap: GameMap,
    private val camera: GameCamera,
    private val localPlayerId: String
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
        interpolationCarsUpdate(deltaTime)
        camera.update(deltaTime)

//        this.state = state.copy(players = state.players)
        return this.state
    }

    // Обновление информации с сервера
    fun applyServerState(serverPlayerStates: List<PlayerStateDto>) {
        val serverPlayerIds: Set<String> = serverPlayerStates.map { it.id }.toSet()

        serverPlayerStates.forEach { serverPlayer ->
            val clientPlayer: Car? = state.players.find { it.id == serverPlayer.id }

            if (clientPlayer != null) {
                correctPlayers(clientPlayer, serverPlayer)
            } else {
                val newCar = Car(
                    id = serverPlayer.id,
                    // TODO: передавать имя с сервера
                    playerName = "Player ${serverPlayer.id.take(2)}",
                    isPlayer = false,
                    isMultiplayer = true,
                    initialPosition = Offset(serverPlayer.posX, serverPlayer.posY)
                )
                state = state.copy(players = state.players + newCar)
                remoteCarTargets[serverPlayer.id] = newCar.position
            }
        }

        removeDisconnectedPlayers(serverPlayerIds)
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
            val cellX = car.position.x.toInt().coerceIn(0, gameMap.size - 1)
            val cellY = car.position.y.toInt().coerceIn(0, gameMap.size - 1)

            // Чужие машины - плавно двигаем их к целевой позиции от сервера (ИНТЕРПОЛЯЦИЯ)
            if (car.id != localPlayerId) {
                val targetPosition = remoteCarTargets[car.id] ?: car.position
                car.position = lerp(car.position, targetPosition, interpolation)
            }

            // Своя машина - плавно обновляем перемещение
            car.setSpeedModifier(gameMap.getSpeedModifier(cellX, cellY))
            car.update(deltaTime)
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

    private  fun correctPlayers(clientPlayer: Car, serverPlayer:  PlayerStateDto) {
        if (clientPlayer.id == localPlayerId) {
            correctClientPlayerPos(clientPlayer, serverPlayer, 0.5f)
        } else {
            correctRemouteCarTargets(clientPlayer, serverPlayer)
        }
    }

    private fun correctRemouteCarTargets(clientPlayer: Car, serverPlayer: PlayerStateDto) {
        val serverPosition = Offset(serverPlayer.posX, serverPlayer.posY)
        remoteCarTargets[serverPlayer.id] = serverPosition
        clientPlayer.visualDirection = serverPlayer.direction
    }

    private fun removeDisconnectedPlayers(serverPlayerIds: Set<String>) {
        val currentPlayers = state.players
        val disconnectedPlayers = currentPlayers.filterNot { serverPlayerIds.contains(it.id) }
        if (disconnectedPlayers.isNotEmpty()) {
            state = state.copy(players = currentPlayers - disconnectedPlayers.toSet())
            disconnectedPlayers.forEach { remoteCarTargets.remove(it.id) }
        }
    }
}