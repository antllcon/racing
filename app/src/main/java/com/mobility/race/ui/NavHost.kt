package com.mobility.race.ui

import SingleplayerGameScreen
import SoundManager
import android.content.Context
import android.content.pm.ActivityInfo
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.mobility.race.data.PlayerResultStorage
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
object MultiplayerRaceFinished

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
    val activity = LocalActivity.current
    var orientation by remember { mutableStateOf(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) }

    LaunchedEffect(orientation) {
        activity?.requestedOrientation = orientation
    }

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
            orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            MenuScreen(
                navigateToSingleplayer = { navController.navigate(route = SingleplayerGame) },
                navigateToMultiplayerMenuScreen = { navController.navigate(route = MultiplayerMenuScreen) },
                soundManager = soundManager
            )
        }

        composable<MultiplayerMenuScreen> {
            orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            MultiplayerMenuScreen(
                navigateToJoinRoom = { playerName ->
                    navController.navigate(route = EnterRoom(playerName))
                },
                navigateToCreateRoom = { playerName, roomName ->
                    navController.navigate(route = Room(playerName, roomName, true))
                },
                onBack = {
                    navController.popBackStack()
                },
                soundManager = soundManager
            )
        }

        composable<SingleplayerGame> {
            orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            SingleplayerGameScreen(
                navigateToFinished = { time, laps, total ->
                    navController.navigate(RaceFinished(time, laps, total)) {
                        popUpTo(SingleplayerGame) { inclusive = true }
                    }
                },
                onBack = {
                    navController.navigate(route = Menu) {
                        popUpTo(Menu) { inclusive = true }
                    }
                }
            )
        }

        composable<EnterRoom> { entry ->
            orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            val args = entry.toRoute<EnterRoom>()

            EnterRoomScreen(
                playerName = args.name,
                navigateToRoom = { playerName, roomName ->
                    navController.navigate(route = Room(playerName, roomName, false))
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Room> { entry ->
            orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            val args = entry.toRoute<Room>()

            val factory = remember(gateway) {
                RoomViewModelFactory(
                    args.playerName,
                    args.roomName,
                    args.isCreatingRoom,
                    context,
                    navController,
                    gateway
                )
            }
            val viewModel: RoomViewModel = viewModel(factory = factory)

            RoomScreen(
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<MultiplayerGame>(
            typeMap = mapOf(
                typeOf<Map<String, String>>() to MapStringType
            )
        ) { entry ->
            orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            val args = entry.toRoute<MultiplayerGame>()
            val context = LocalContext.current

            val factory = remember(gateway) {
                MultiplayerGameViewModelFactory(
                    args.nickname,
                    args.playerNames,
                    args.playerSpriteId,
                    context,
                    gateway
                )
            }

            val viewModel: MultiplayerGameViewModel = viewModel(factory = factory)
            viewModel.onFinish = { navController.navigate(route = MultiplayerRaceFinished) }
            viewModel.onError = {navController.navigate(route = Menu)}
            val soundManager = remember { SoundManager(context) }

            MultiplayerGameScreen(
                viewModel = viewModel,
                soundManager = soundManager,
                onBack = {
                    navController.navigate(route = Menu) {
                        popUpTo(Menu) { inclusive = true }
                    }
                }
            )
        }

        composable<RaceFinished> { entry ->
            orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
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

        composable<MultiplayerRaceFinished> {
            orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

            MultiplayerRaceFinishedScreen(
                playerResults = PlayerResultStorage.results,
                onRestart = {
                    navController.navigate(MultiplayerMenuScreen) {
                        popUpTo(MultiplayerMenuScreen) { inclusive = true }
                    }
                    PlayerResultStorage.results = emptyList()
                },
                onExit = {
                    navController.navigate(route = Menu) {
                        popUpTo(Menu) { inclusive = true }
                    }
                    PlayerResultStorage.results = emptyList()
                },
                soundManager = soundManager
            )
        }
    }
}