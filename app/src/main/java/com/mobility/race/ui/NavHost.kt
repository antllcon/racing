package com.mobility.race.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.mobility.race.presentation.SingleplayerGameViewModel
import kotlinx.serialization.Serializable

//Leave them there for now,
//but do not forget that they are not supposed to be here
@Serializable
object Menu

@Serializable
object SingleplayerGame

@Serializable
object EnterRoom

@Serializable
data class MultiplayerGame(
    val roomName: String
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
                navigateToSingleplayer = { navController.navigate(SingleplayerGame) },
                navigateToJoinRoom = { navController.navigate(EnterRoom) }
            )
        }

        composable<SingleplayerGame> {
            val viewModel = SingleplayerGameViewModel()

            SingleplayerGameScreen(viewModel)
        }

        composable<EnterRoom> {
            EnterRoomScreen(navigateToMultiplayer = { roomName ->
                navController.navigate(MultiplayerGame(roomName))
            }
            )
        }

        composable<MultiplayerGame> { entry ->
            val name = entry.toRoute<MultiplayerGame>().roomName

            MultiplayerGameScreen(
                roomName = name
            )
        }
    }
}