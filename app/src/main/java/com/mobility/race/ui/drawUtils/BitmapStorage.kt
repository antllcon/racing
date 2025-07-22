package com.mobility.race.ui.drawUtils

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import com.mobility.race.R

@Composable
fun bitmapStorage(): Map<String, ImageBitmap> {
    return mutableMapOf(
        "car1_1" to ImageBitmap.imageResource(R.drawable.car1_1),
        "car1_2" to ImageBitmap.imageResource(R.drawable.car1_2),
        "car1_3" to ImageBitmap.imageResource(R.drawable.car1_3),
        "car1_4" to ImageBitmap.imageResource(R.drawable.car1_4),
        "car2_1" to ImageBitmap.imageResource(R.drawable.car2_1),
        "car2_2" to ImageBitmap.imageResource(R.drawable.car2_2),
        "car2_3" to ImageBitmap.imageResource(R.drawable.car2_3),
        "car2_4" to ImageBitmap.imageResource(R.drawable.car2_4),
        "car3_1" to ImageBitmap.imageResource(R.drawable.car3_1),
        "car3_2" to ImageBitmap.imageResource(R.drawable.car3_2),
        "car3_3" to ImageBitmap.imageResource(R.drawable.car3_3),
        "car3_4" to ImageBitmap.imageResource(R.drawable.car3_4),
        "car4_1" to ImageBitmap.imageResource(R.drawable.car4_1),
        "car4_2" to ImageBitmap.imageResource(R.drawable.car4_2),
        "car4_3" to ImageBitmap.imageResource(R.drawable.car4_3),
        "car4_4" to ImageBitmap.imageResource(R.drawable.car4_4),
        "car5_1" to ImageBitmap.imageResource(R.drawable.car5_1),
        "car5_2" to ImageBitmap.imageResource(R.drawable.car5_2),
        "car5_3" to ImageBitmap.imageResource(R.drawable.car5_3),
        "car5_4" to ImageBitmap.imageResource(R.drawable.car5_4),
        "car6_1" to ImageBitmap.imageResource(R.drawable.car6_1),
        "car6_2" to ImageBitmap.imageResource(R.drawable.car6_2),
        "car6_3" to ImageBitmap.imageResource(R.drawable.car6_3),
        "car6_4" to ImageBitmap.imageResource(R.drawable.car6_4),
        "terrain_000" to ImageBitmap.imageResource(R.drawable.terrain_000),
        "terrain_010" to ImageBitmap.imageResource(R.drawable.terrain_010),
        "terrain_201" to ImageBitmap.imageResource(R.drawable.terrain_201),
        "terrain_202" to ImageBitmap.imageResource(R.drawable.terrain_202),
        "terrain_203" to ImageBitmap.imageResource(R.drawable.terrain_203),
        "terrain_204" to ImageBitmap.imageResource(R.drawable.terrain_204),
        "terrain_205" to ImageBitmap.imageResource(R.drawable.terrain_205),
        "terrain_206" to ImageBitmap.imageResource(R.drawable.terrain_206),
        "terrain_211" to ImageBitmap.imageResource(R.drawable.terrain_211),
        "terrain_212" to ImageBitmap.imageResource(R.drawable.terrain_212),
        "terrain_213" to ImageBitmap.imageResource(R.drawable.terrain_213),
        "terrain_214" to ImageBitmap.imageResource(R.drawable.terrain_214),
        "terrain_215" to ImageBitmap.imageResource(R.drawable.terrain_215),
        "terrain_216" to ImageBitmap.imageResource(R.drawable.terrain_216)
    )
}