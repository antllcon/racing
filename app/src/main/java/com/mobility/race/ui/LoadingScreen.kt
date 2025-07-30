package com.mobility.race.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobility.race.ui.drawUtils.LockScreenOrientation
import com.mobility.race.ui.drawUtils.Orientation


@Composable
fun LoadingScreen() {
    LockScreenOrientation(Orientation.LANDSCAPE)
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            color = Color.Gray
        )

        Text(
            text = "Loading game...",
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}