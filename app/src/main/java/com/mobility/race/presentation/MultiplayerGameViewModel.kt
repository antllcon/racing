package com.mobility.race.presentation

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobility.race.data.AppJson
import com.mobility.race.data.ErrorResponse
import com.mobility.race.data.Gateway
import com.mobility.race.data.InfoResponse
import com.mobility.race.data.JoinedRoomResponse
import com.mobility.race.data.LeftRoomResponse
import com.mobility.race.data.PlayerActionResponse
import com.mobility.race.data.PlayerConnectedResponse
import com.mobility.race.data.PlayerDisconnectedResponse
import com.mobility.race.data.RoomCreatedResponse
import com.mobility.race.data.RoomUpdatedResponse
import com.mobility.race.data.ServerMessage
import com.mobility.race.domain.Car
import com.mobility.race.domain.GameCamera
import com.mobility.race.domain.GameMap
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import kotlinx.coroutines.flow.asStateFlow
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MultiplayerGameViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel(), IGameplay {

    private val _playerName: String = checkNotNull(savedStateHandle["playerName"])
    private val _roomName: String = checkNotNull(savedStateHandle["roomName"])
    private val _isCreatingRoom: Boolean = checkNotNull(savedStateHandle["isCreatingRoom"])


    private val _httpClient: HttpClient = HttpClient(engineFactory = CIO) {
        install(plugin = WebSockets)
        install(plugin = ContentNegotiation) {
            json(AppJson)
        }
    }

    private val _gateway: Gateway = Gateway(
        client = _httpClient,
        serverHost = "130.193.44.108",
        serverPort = 8080,
        serverPath = "/",
        onServerMessage = ::handleServerMessage,
        onError = ::handleGatewayError
    )

    private val _players = MutableStateFlow<List<Car>>(value = emptyList())
    val players: StateFlow<List<Car>> = _players.asStateFlow()

    private val _gameStatus = MutableStateFlow(value = "Not Started")
    val gameStatus: StateFlow<String> = _gameStatus.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(value = null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                _gateway.connect()
                delay(1000)
                _gateway.initPlayer(_playerName)
                delay(1000)

                if (_isCreatingRoom) {
                    _gateway.createRoom(_roomName)
                } else {
                    _gateway.joinRoom(_roomName)
                }
                _gateway.createRoom(_roomName)
                delay(1000)

                println("ViewModel: Player $_playerName create room --> $_roomName")

            } catch (e: Exception) {
                println("ViewModel: Error during initial setup: ${e.message}")
                _errorMessage.value = "Initial setup failed: ${e.message}"
            }
        }
    }

    override fun init(playerCar: Car, playerGameMap: GameMap, playerCamera: GameCamera) {
        println("ViewModel: IGameplay.init() called")
        // Здесь можно инициализировать локальное состояние игры, которое не зависит от сети
        // Например, сохранить playerCar, playerGameMap, playerCamera для рендеринга
    }

    override fun runGame() {
        println("ViewModel: IGameplay.runGame called. Game logic should now start.")
        println("ViewModel: try to join room $_roomName")
        // Здесь можно писать игровую логику, которая будет использовать сетевые данные
    }

    override fun movePlayer(elapsedTime: Float) {
        TODO("Not yet implemented")
    }

    override fun moveCamera(elapsedTime: Float) {
        TODO("Not yet implemented")
    }

    override fun setTouchPosition(newTouchPosition: Offset?) {
        TODO("Not yet implemented")
    }

    override fun stopGame() {
        println("ViewModel: try to leave room $_roomName")
        viewModelScope.launch {
            _gateway.leaveRoom()
        }
    }

    private fun handleServerMessage(message: ServerMessage) {
        viewModelScope.launch {
            when (message) {
                is PlayerConnectedResponse -> {
                    println("ViewModel: Player ${message.playerName} connected with ID: ${message.playerId}")
                    _players.value = _players.value + Car(
                        playerName = message.playerName,
                        isPlayer = false,
                        isMultiplayer = true,
                        id = message.playerId
                    )
                }

                is PlayerDisconnectedResponse -> {
                    println("ViewModel: Player ${message.playerId} disconnected")
                    _players.value = _players.value.filter { it.id != message.playerId }
                }

                is InfoResponse -> {
                    println("ViewModel: Info: ${message.message}")
                    _gameStatus.value = message.message
                }

                is ErrorResponse -> {
                    println("ViewModel: server send error: ${message.code}")
                    _errorMessage.value = "${message.code}: ${message.message}"
                }

                is RoomCreatedResponse -> {
                    println("ViewModel: Room ${message.roomId} created")
                    _gameStatus.value = "Room ${message.roomId} created"
                }

                is JoinedRoomResponse -> {
                    println("ViewModel: Successfully joined room: ${message.roomId}")
                    _gameStatus.value = "Joined room: ${message.roomId}"
                }

                is LeftRoomResponse -> {
                    println("ViewModel: Left room: ${message.roomId}")
                    _gameStatus.value = "Left room: ${message.roomId}"
                    _players.value = emptyList()
                }

                is RoomUpdatedResponse -> {
                    println("ViewModel: Room ${message.roomId} updated. (TODO: process detailed room state)")
                    // Возможно обработка состояния комнаты,
                    // например, список всех игроков, их позиции и т.д.
                }

                is PlayerActionResponse -> {
                    println("ViewModel: Player action response: ${message.name}")
                }
            }
        }
    }

    private fun handleGatewayError(errorMessage: String) {
        viewModelScope.launch {
            println("ViewModel: Gateway error")
            _errorMessage.value = "Connection error: $errorMessage"
        }
    }

    override fun onCleared() {
        super.onCleared()
        println("ViewModel: disconnect Gateway and close HttpClient")
        viewModelScope.launch {
            _gateway.disconnect()
            _httpClient.close()
        }
    }
}