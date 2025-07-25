package com.mobility.race.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.mobility.race.data.AppJson
import com.mobility.race.presentation.MultiplayerGameViewModelFactory
import com.mobility.race.presentation.multiplayer.MultiplayerGameViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable

@Serializable
object Menu

@Serializable
object SingleplayerGame

@Serializable
object EnterRoom

@Serializable
object CreateRoom

@Serializable
object RaceFinished

@Serializable
data class MultiplayerGame(
    val playerName: String,
    val roomName: String,
    val isCreatingRoom: Boolean
)

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
    val httpClient = remember {
        HttpClient(CIO) {
            install(WebSockets)
            install(ContentNegotiation) {
                json(AppJson)
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Menu
    ) {
        composable<Menu> {
            MenuScreen(
                navigateToSingleplayer = { navController.navigate(route = SingleplayerGame) },
                navigateToCreateRoom = { navController.navigate(route = CreateRoom) },
                navigateToJoinRoom = { navController.navigate(route = EnterRoom) }
            )
        }

        composable<SingleplayerGame> {
            SingleplayerGameScreen(
                navigateToFinished = { time, laps, total ->
                    navController.navigate(RaceFinished) {
                        popUpTo(Menu) { inclusive = false }
                    }
                }
            )
        }

        composable<CreateRoom> {
            CreateRoomScreen(navigateToMultiplayer = { playerName, roomName ->
                navController.navigate(route = MultiplayerGame(playerName, roomName, true))
            })
        }

        composable<EnterRoom> {
            EnterRoomScreen(navigateToMultiplayer = { playerName, roomName ->
                navController.navigate(route = MultiplayerGame(playerName, roomName, false))
            })
        }

        composable<MultiplayerGame> { entry ->
            val args = entry.toRoute<MultiplayerGame>()

            val factory = remember(httpClient) {
                MultiplayerGameViewModelFactory(httpClient)
            }

            val viewModel: MultiplayerGameViewModel = viewModel(factory = factory)

            MultiplayerGameScreen(
                playerName = args.playerName,
                roomName = args.roomName,
                isCreatingRoom = args.isCreatingRoom,
                viewModel = viewModel
            )
        }
        composable<RaceFinished> {
            RaceFinishedScreen(
                finishTime = 0L,
                lapsCompleted = 0,
                totalLaps = 0,
                onRestart = {
                    navController.popBackStack()
                },
                onExit = {
                    navController.navigate(route = Menu) {
                        popUpTo(Menu) { inclusive = true }
                    }
                }
            )
        }
    }
}