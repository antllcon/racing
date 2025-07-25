package com.mobility.race.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobility.race.R

@Composable
fun MenuScreen(
    navigateToSingleplayer: () -> Unit,
    navigateToMultiplayerMenuScreen: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxSize().paint(
            painterResource(R.drawable.background_image),
            contentScale = ContentScale.FillBounds,
            alpha = 0.4f
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painterResource(R.drawable.app_icon),
                modifier = Modifier.size(175.dp),
                contentDescription = "AppIcon"
            )

            Text(text = "Just Race!",
                modifier = Modifier.padding(vertical = 20.dp),
                fontSize = 60.sp,
                color = Color(0xFFBC13FE),
                fontFamily = FontFamily(Font(R.font.jersey25)),
                fontWeight = FontWeight(200)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Button(onClick = navigateToSingleplayer)
            {
                Text(text = "Singleplayer Game")
            }

            Button(onClick = navigateToMultiplayerMenuScreen)
            {
                Text(text = "Multiplayer Game")
            }
        }
    }
}