package com.mobility.race.presentation

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobility.race.Server
import com.mobility.race.data.ErrorResponse
import com.mobility.race.data.Gateway
import com.mobility.race.data.IGateway
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MultiplayerGameViewModel(
    savedStateHandle: SavedStateHandle,
    private val httpClient: HttpClient
) : ViewModel() {

    private val _gateway: IGateway = Gateway(
        client = httpClient,
        serverConfig = Server.default(),
        onServerMessage = this::handleServerMessage,
        onError = this::handleGatewayError
    )

    private val _playerName: String = checkNotNull(savedStateHandle["playerName"])
    private val _roomName: String = checkNotNull(savedStateHandle["roomName"])
    private val _isCreatingRoom: Boolean = checkNotNull(savedStateHandle["isCreatingRoom"])

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

            } catch (e: Exception) {
                println("ViewModel: Error during initial setup: ${e.message}")
                _errorMessage.value = "Initial setup failed: ${e.message}"
            }
        }
    }

    fun init(playerCar: Car, playerGameMap: GameMap, playerCamera: GameCamera) {
        println("ViewModel: init")
    }

    fun runGame() {
        println("ViewModel: run game")
    }

    fun stopGame() {
        println("ViewModel: try to leave room $_roomName")
        viewModelScope.launch {
            _gateway.leaveRoom()
        }
    }

    fun movePlayer(touchCoordinates: Offset) {
        println("ViewModel: Player move action at $touchCoordinates")
        viewModelScope.launch {
            _gateway.playerAction("Move to X:${touchCoordinates.x}, Y:${touchCoordinates.y}")
        }
    }

    fun handleServerMessage(message: ServerMessage) {
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
                    println("ViewModel: Room ${message.roomId} updated")
                }

                is PlayerActionResponse -> {
                    println("ViewModel: Player action response: ${message.name}")
                }
            }
        }
    }

    fun handleGatewayError(errorMessage: String) {
        viewModelScope.launch {
            println("ViewModel: Gateway error: $errorMessage")
            _errorMessage.value = "Connection error: $errorMessage"
        }
    }

    override fun onCleared() {
        super.onCleared()
        println("ViewModel: disconnect Gateway and close HttpClient")
        viewModelScope.launch {
            _gateway.disconnect()
            httpClient.close()
        }
    }
}