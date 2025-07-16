package com.mobility.race.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobility.race.data.Server
import com.mobility.race.data.ErrorResponse
import com.mobility.race.data.GameStateUpdateResponse
import com.mobility.race.data.Gateway
import com.mobility.race.data.IGateway
import com.mobility.race.data.InfoResponse
import com.mobility.race.data.JoinedRoomResponse
import com.mobility.race.data.LeftRoomResponse
import com.mobility.race.data.PlayerActionResponse
import com.mobility.race.data.PlayerConnectedResponse
import com.mobility.race.data.PlayerDisconnectedResponse
import com.mobility.race.data.PlayerInputRequest
import com.mobility.race.data.RoomCreatedResponse
import com.mobility.race.data.RoomUpdatedResponse
import com.mobility.race.data.ServerMessage
import com.mobility.race.domain.Car
import com.mobility.race.domain.GameCamera
import com.mobility.race.domain.GameMap
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.atan2

class MultiplayerGameViewModel(
    savedStateHandle: SavedStateHandle,
    private val httpClient: HttpClient
) : ViewModel(), IGameplay {

    private val _gateway: IGateway = Gateway(
        client = httpClient,
        serverConfig = Server.default(),
        onServerMessage = this::handleServerMessage,
        onError = this::handleGatewayError
    )

    // Текущий игрок, комната
    private val _playerName: String = checkNotNull(savedStateHandle["playerName"])
    private val _roomName: String = checkNotNull(savedStateHandle["roomName"])
    private val _isCreatingRoom: Boolean = checkNotNull(savedStateHandle["isCreatingRoom"])

    // Массивы
    private val _players = MutableStateFlow<List<Car>>(value = emptyList())
    private val _gameStatus = MutableStateFlow(value = "Not Started")
    val gameStatus: StateFlow<String> = _gameStatus.asStateFlow()
    private val _errorMessage = MutableStateFlow<String?>(value = null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Состояние всей игры для UI
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private var _touchPosition = mutableStateOf(Offset(0f, 0f))

    // Игровой движок
    private lateinit var _gameEngine: GameEngine
    private var _gameLoopJob: Job? = null

    private var _playerInput = PlayerInput()

    lateinit var camera: GameCamera
        private set

    lateinit var gameMap: GameMap
        private set

    lateinit var car: Car
        private set

    init {
        viewModelScope.launch {
            try {
                val defaultCar = Car(id = "temp_id", playerName = _playerName)
                val defaultGameMap = GameMap.createRaceTrackMap()
                val defaultGameCamera = GameCamera(defaultCar, Size.Zero)

                gameMap = defaultGameMap
                camera = defaultGameCamera
                car = defaultCar

                _gameEngine = GameEngine(
                    gameMap = gameMap,
                    camera = camera,
                    localPlayerId = car.id
                )

                _gateway.connect()
                delay(1000)
                _gateway.initPlayer(_playerName)
                delay(1000)

                if (_isCreatingRoom) {
                    _gateway.createRoom(_roomName)
                } else {
                    _gateway.joinRoom(_roomName)
                }
                delay(1000)

            } catch (e: Exception) {
                println("ViewModel: Error during initial setup: ${e.message}")
                _errorMessage.value = "Initial setup failed: ${e.message}"
            }
        }
    }

    override fun init(playerCar: Car, playerGameMap: GameMap, playerCamera: GameCamera) {
        this._gameEngine = GameEngine(
            gameMap = playerGameMap,
            camera = playerCamera,
            localPlayerId = playerCar.id
        )

        this.car = playerCar
        this.gameMap = playerGameMap
        this.camera = playerCamera

        _gameState.value = GameState(
            players = listOf(playerCar),
            localPlayerId = playerCar.id
        )
    }

    override fun runGame() {
        if (!this::_gameEngine.isInitialized) {
            println("ViewModel: GameEngine not initialized, cannot run game.")
            _errorMessage.value = "Game engine not ready."
            return
        }

        _gameLoopJob?.cancel()
        _gameLoopJob = viewModelScope.launch(Dispatchers.Default) {
            var lastTime = System.currentTimeMillis()
            while (isActive) {
                val currentTime = System.currentTimeMillis()
                val deltaTime = (currentTime - lastTime) / 1000f
                lastTime = currentTime

                // Получаем текущий ввод от игрока (из movePlayer)
                val playerInput = calculatePlayerInput()

                // Обновляем движок и получаем новое состояние
                val newState = _gameEngine.update(deltaTime, playerInput)

                // Отправляем новое состояние в UI
                _gameState.value = newState

                delay(16)
            }
        }
    }

    override fun movePlayer(touchCoordinates: Offset) {
        if (!::camera.isInitialized || !::car.isInitialized) return

        val isAccelerating: Boolean = touchCoordinates != Offset.Zero
        var turnDirection = 0f

        if (isAccelerating) {
            val worldRouchPos = camera.screenToWorld(touchCoordinates)
            val angle = atan2(
                worldRouchPos.y - car.position.y,
                worldRouchPos.x - car.position.x
            )
            var diff = angle - car.direction

            while (diff > Math.PI) diff -= 2 * Math.PI.toFloat()
            while (diff < -Math.PI) diff += 2 * Math.PI.toFloat()

            if (abs(diff) < Math.PI.toFloat() / 2) {
                turnDirection = (diff / (Math.PI.toFloat() / 2)).coerceIn(-1f, 1f)
            }
        }

        // Сохраняем ввод
        this._playerInput = PlayerInput(isAccelerating, turnDirection)

        // Отправляем на сервер
        viewModelScope.launch {
            _gateway.playerAction(
                PlayerInputRequest(isAccelerating, turnDirection)
            )
        }
    }

    fun handleServerMessage(message: ServerMessage) {
        viewModelScope.launch {
            when (message) {
                is PlayerConnectedResponse -> {
                    println("ViewModel: Player ${message.playerName} connected with ID: ${message.playerId}")
                }

                is PlayerDisconnectedResponse -> {
                    println("ViewModel: Player ${message.playerId} disconnected")
                }

                is GameStateUpdateResponse -> {
                    _gameEngine.applyServerState(message.players)
                }

//                is PlayerConnectedResponse -> {
//                    println("ViewModel: Player ${message.playerName} connected with ID: ${message.playerId}")
//                    _players.value = _players.value + Car(
//                        playerName = message.playerName,
//                        isPlayer = false,
//                        isMultiplayer = true,
//                        id = message.playerId
//                    )
//                }
//
//                is PlayerDisconnectedResponse -> {
//                    println("ViewModel: Player ${message.playerId} disconnected")
//                    _players.value = _players.value.filter { it.id != message.playerId }
//                }

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

    override fun stopGame() {
        println("ViewModel: try to leave room $_roomName")
        viewModelScope.launch {
            _gateway.leaveRoom()
        }
    }

    private fun calculatePlayerInput(): PlayerInput {
        return this._playerInput
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