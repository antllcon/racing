package com.mobility.race.ui

import SingleplayerGameScreen
import SoundManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.mobility.race.data.AppJson
import com.mobility.race.data.Gateway
import com.mobility.race.data.IGateway
import com.mobility.race.data.MapStringType
import com.mobility.race.data.Server
import com.mobility.race.di.MultiplayerGameViewModelFactory
import com.mobility.race.di.RoomViewModelFactory
import com.mobility.race.presentation.multiplayer.MultiplayerGameViewModel
import com.mobility.race.presentation.multiplayer.RoomViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf

@Serializable
object Menu

@Serializable
object SingleplayerGame

@Serializable
data class EnterRoom(
    val name: String
)

@Serializable
data class RaceFinished(
    val finishTime: Long = 0L,
    val lapsCompleted: Int = 0,
    val totalLaps: Int = 0
)

@Serializable
data class Room(
    val playerName: String,
    val roomName: String,
    val isCreatingRoom: Boolean
)

@Serializable
object MultiplayerMenuScreen

@Serializable
data class MultiplayerGame(
    val nickname: String,
    val playerNames: Array<String>,
    val playerSpriteId: String
)

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    context: Context = LocalContext.current,
    soundManager: SoundManager = remember { SoundManager(context) }
) {
    val httpClient = remember {
        HttpClient(CIO) {
            install(WebSockets)
            install(ContentNegotiation) {
                json(AppJson)
            }
        }
    }

    val gateway: IGateway = Gateway(
        client = httpClient,
        serverConfig = Server.default()
    )

    NavHost(
        navController = navController,
        startDestination = Menu
    ) {
        composable<Menu> {
            MenuScreen(
                navigateToSingleplayer = { navController.navigate(route = SingleplayerGame) },
                navigateToMultiplayerMenuScreen = { navController.navigate(route = MultiplayerMenuScreen) },
                soundManager = soundManager
            )
        }

        composable<MultiplayerMenuScreen> {
            MultiplayerMenuScreen(
                navigateToJoinRoom = { playerName ->
                    navController.navigate(route = EnterRoom(playerName))
                },
                navigateToCreateRoom = { playerName, roomName ->
                    navController.navigate(route = Room(playerName, roomName, true))
                }
            )
        }

        composable<SingleplayerGame> {
            SingleplayerGameScreen(
                navigateToFinished = { time, laps, total ->
                    navController.navigate(RaceFinished(time, laps, total)) {
                        popUpTo(SingleplayerGame) { inclusive = true }
                    }
                },
                onExit = {
                    navController.navigate(route = Menu) {
                        popUpTo(Menu) { inclusive = true }
                    }
                },
                onRestart = {
                }
            )
        }

        composable<EnterRoom> { entry ->
            val args = entry.toRoute<EnterRoom>()

            EnterRoomScreen(playerName = args.name, navigateToRoom = { playerName, roomName ->
                navController.navigate(route = Room(playerName, roomName, false))
            })
        }

        composable<Room> { entry ->
            val args = entry.toRoute<Room>()

            val factory = remember(gateway) {
                RoomViewModelFactory(
                    args.playerName,
                    args.roomName,
                    args.isCreatingRoom,
                    navController,
                    gateway
                )
            }
            val viewModel: RoomViewModel = viewModel(factory = factory)

            RoomScreen(viewModel = viewModel)
        }

        composable<MultiplayerGame>(
            typeMap = mapOf(
                typeOf<Map<String, String>>() to MapStringType
            )
        ) { entry ->
            val args = entry.toRoute<MultiplayerGame>()

            val factory = remember(gateway) {
                MultiplayerGameViewModelFactory(
                    args.nickname,
                    args.playerNames,
                    args.playerSpriteId,
                    gateway
                )
            }

            val viewModel: MultiplayerGameViewModel = viewModel(factory = factory)
            val context = LocalContext.current
            val soundManager = remember { SoundManager(context) }

            MultiplayerGameScreen(
                viewModel = viewModel,
                soundManager = soundManager
            )
        }

        composable<RaceFinished> { entry ->
            val args = entry.toRoute<RaceFinished>()
            val context = LocalContext.current
            val soundManager = remember { SoundManager(context) }

            RaceFinishedScreen(
                finishTime = args.finishTime,
                lapsCompleted = args.lapsCompleted,
                totalLaps = args.totalLaps,
                onRestart = {
                    navController.navigate(SingleplayerGame) {
                        popUpTo(SingleplayerGame) { inclusive = true }
                    }
                },
                onExit = {
                    navController.navigate(route = Menu) {
                        popUpTo(Menu) { inclusive = true }
                    }
                },
                soundManager = soundManager
            )
        }
    }
}