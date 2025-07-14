package com.mobility.race.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.mobility.race.presentation.MultiplayerGameViewModel
import com.mobility.race.presentation.SingleplayerGameViewModel
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
data class MultiplayerGame(
    val playerName: String,
    val roomName: String,
    val isCreatingRoom: Boolean
)

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
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
            val viewModel = SingleplayerGameViewModel()

            SingleplayerGameScreen(viewModel)
        }

        composable<EnterRoom> {
            EnterRoomScreen(navigateToMultiplayer = { playerName, roomName ->
                navController.navigate(route = MultiplayerGame(playerName, roomName, false))
            })
        }

        composable<CreateRoom> {
            CreateRoomScreen(navigateToMultiplayer = { playerName, roomName ->
                navController.navigate(route = MultiplayerGame(playerName, roomName, true))
            })
        }

        composable<MultiplayerGame> { entry ->
            val roomName: String = entry.toRoute<MultiplayerGame>().roomName
            val playerName: String = entry.toRoute<MultiplayerGame>().playerName
            val isCreatingRoom: Boolean = entry.toRoute<MultiplayerGame>().isCreatingRoom
            val viewModel: MultiplayerGameViewModel = viewModel()

            MultiplayerGameScreen(
                playerName = playerName,
                roomName = roomName,
                isCreatingRoom = isCreatingRoom,
                viewModel = viewModel
            )
        }
    }
}