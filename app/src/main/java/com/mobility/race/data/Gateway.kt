package com.mobility.race.data

import com.mobility.race.Server
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.URLProtocol
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException

class Gateway(
    private val client: HttpClient,
    private val serverConfig: Server,
    private val onServerMessage: (ServerMessage) -> Unit,
    private val onError: (String) -> Unit
) : IGateway {
    private var session: WebSocketSession? = null
    private var job: Job? = null

    override suspend fun connect() {
        val protocolString = if (serverConfig.port == 443) "wss" else "ws"
        println("Gateway: try connect to $protocolString://${serverConfig.host}:${serverConfig.port}${serverConfig.path}")

        try {
            session = client.webSocketSession(host = serverConfig.host, path = serverConfig.path) {
                url {
                    protocol = if (serverConfig.port == 443) URLProtocol.WSS else URLProtocol.WS
                    port = serverConfig.port
                }
            }
            job = startGettingMessages(session!!)

        } catch (e: Exception) {
            onError("Failed to connect to server: ${e.message}")
            println("Gateway: fail to connect server")
        }
    }

    override suspend fun disconnect() {
        println("Gateway: try disconnect")

        try {
            job?.cancelAndJoin()
            session?.close()

        } catch (e: Exception) {
            onError("Error during disconnect: ${e.message}")
            println("Gateway: Error during disconnect")

        } finally {
            session = null
            job = null
            println("Gateway: disconnect from server")
        }
    }

    private suspend fun sendMessage(message: ClientMessage) {
        println("Gateway: try to send message: $message")

        if (session?.isActive == true) {
            try {
                sendToSession(session!!, message)

            } catch (e: Exception) {
                onError("Failed to send message: ${e.message}")
                println("Gateway: fail to send message")
            }
        } else {
            onError("Gateway is not connected or session is inactive. Cannot send message: $message")
            println("Gateway: Not connected or session inactive, cannot send message")
        }
    }

    override suspend fun initPlayer(name: String) {
        println("Gateway: init player with name: $name")
        sendMessage(InitPlayerRequest(name = name))
    }

    override suspend fun createRoom(name: String) {
        println("Gateway: create room with name: $name")
        sendMessage(CreateRoomRequest(name = name))
    }

    override suspend fun joinRoom(name: String) {
        println("Gateway: join room with name: $name")
        sendMessage(JoinRoomRequest(name = name))
    }

    override suspend fun leaveRoom() {
        println("Gateway: Sending LeaveRoomRequest.")
        sendMessage(LeaveRoomRequest)
    }

    override suspend fun playerAction(name: String) {
        println("Gateway: player action with name: $name")
        sendMessage(JoinRoomRequest(name = name))
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
                    onError("Error from WebSocket: ${e.message}")
                    println("Gateway: Error from WebSocket")
                }
            } finally {
                println("Gateway: Job finished")
            }
        }
    }

    private fun processIncomingFrame(frame: Frame) {
        if (frame is Frame.Text) {
            val text = frame.readText()
            try {
                val serverMessage: ServerMessage =
                    AppJson.decodeFromString<ServerMessage>(string = text)
                onServerMessage(serverMessage)
            } catch (e: Exception) {
                onError("Error Deserializing server message: ${e.message}")
                println("Gateway: Error deserializing server message")
            }
        }
    }
}