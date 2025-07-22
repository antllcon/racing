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
        "car3_4" to ImageBitmap.imageResource(R.drawable.car3_4)
    )
}