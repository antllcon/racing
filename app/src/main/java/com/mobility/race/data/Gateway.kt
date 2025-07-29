package com.mobility.race.data

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.http.URLProtocol
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


class Gateway(
    private val client: HttpClient,
    private val serverConfig: Server,
) : IGateway {
    private var session: WebSocketSession? = null
    private var job: Job? = null
    private lateinit var gatewayStorage: StarterPack

    override val messageFlow: Flow<ServerMessage>
        get() = mMessageFlow.asSharedFlow()

    private val mMessageFlow = MutableSharedFlow<ServerMessage>()

    override fun fillGatewayStorage(starterPack: StarterPack) {
        gatewayStorage = starterPack
    }

    override fun openGatewayStorage(): StarterPack {
        return gatewayStorage
    }

    override suspend fun connect() {
//        val protocol = if (serverConfig.port == 443) "wss" else "ws"

        try {
            session = client.webSocketSession(host = serverConfig.host, path = serverConfig.path) {
                url {
                    protocol = if (serverConfig.port == 443) URLProtocol.WSS else URLProtocol.WS
                    port = serverConfig.port
                }
            }
            job = startGettingMessages(session!!)

        } catch (e: Exception) {
            println("Can't connection to server: ${e.message}")
        }
    }

    override suspend fun disconnect() {

        try {
            job?.cancelAndJoin()
            session?.close()

        } catch (_: Exception) {

        } finally {
            session = null
            job = null
        }
    }

    private suspend fun sendMessage(message: ClientMessage) {

        if (session?.isActive == true) {
            try {
                sendToSession(session!!, message)

            } catch (e: Exception) {
                println("Error sending message: ${e.message}")
            }
        } else {
            // TODO: придумать как выкидывать игрока из комнаты
            println("Session is inactive")
        }
    }

    override suspend fun initPlayer(name: String) {
        sendMessage(InitPlayerRequest(name = name))
    }

    override suspend fun createRoom(name: String) {
        sendMessage(CreateRoomRequest(name = name))
    }

    override suspend fun joinRoom(name: String) {
        sendMessage(JoinRoomRequest(name = name))
    }

    override suspend fun leaveRoom() {
        sendMessage(LeaveRoomRequest)
    }

    override suspend fun playerAction(name: PlayerInputRequest) {
        sendMessage(name)
    }

    override suspend fun startGame(name: String) {
        sendMessage(StartGameRequest(name))
    }

    override suspend fun playerInput(directionAngle: Float?, elapsedTime: Float, ringsCrossed: Int) {
        sendMessage(PlayerInputRequest(directionAngle, elapsedTime, ringsCrossed))
    }

    private fun startGettingMessages(session: WebSocketSession): Job {
        return CoroutineScope(Dispatchers.IO).launch {
            try {
                for (frame in session.incoming) {
                    processIncomingFrame(frame)
                }
            } catch (e: Exception) {
                if (e is ClosedReceiveChannelException) {
                    println("Gateway: WebSocket channel correctly closed")
                } else {
                    println("Gateway: Error from WebSocket")
                }
            } finally {
                println("Gateway: Job finished")
            }
        }
    }

    private suspend fun processIncomingFrame(frame: Frame) {
        if (frame is Frame.Text) {
            val text = frame.readText()
            try {
                val serverMessage: ServerMessage =
                    AppJson.decodeFromString<ServerMessage>(string = text)
                mMessageFlow.emit(serverMessage)
            } catch (_: Exception) {
                println("Gateway: Error deserializing server message")
            }
        }
    }
}