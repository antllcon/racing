package com.mobility.race.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun MultiplayerMenuScreen(
    navigateToJoinRoom: (String) -> Unit,
    navigateToCreateRoom: (String, String) -> Unit
) {
    var playerName: String by remember { mutableStateOf("") }
    var newRoomName: String by remember { mutableStateOf("") }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Button(onClick = {
                if (playerName.isNotEmpty()) {
                    navigateToJoinRoom(playerName)
                }
            })
            {
                Text(text = "Join a Race")
            }

            TextField(
                value = newRoomName,
                onValueChange = {newRoomName = it},
                label = {Text("New room name")}
            )

            Button(onClick = {
                if (newRoomName.isNotEmpty() && playerName.isNotEmpty()) {
                    //TODO: check if room exist

                    navigateToCreateRoom(playerName, newRoomName)
                }
            })
            {
                Text(text = "Create a Race")
            }

            TextField(
                value = playerName,
                onValueChange = { playerName = it },
                label = { Text("Your name") }
            )
        }
    }
}