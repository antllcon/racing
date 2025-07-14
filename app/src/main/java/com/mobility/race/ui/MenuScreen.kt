package com.mobility.race.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun MenuScreen(
    navigateToSingleplayer: () -> Unit,
    navigateToCreateRoom: () -> Unit,
    navigateToJoinRoom: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Button(onClick = navigateToSingleplayer)
        {
            Text(text = "Singleplayer Game")
        }

        Button(onClick = navigateToCreateRoom)
        {
            Text(text = "CreateRoom Game")
        }

        Button(onClick = navigateToJoinRoom) {
            Text(text = "Join room")
        }
    }
}