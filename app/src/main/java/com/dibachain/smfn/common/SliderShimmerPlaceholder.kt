package com.dibachain.smfn.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun SliderShimmerPlaceholder() {
    // نسبت تصویر اسلایدر شما: 360/640  -> height = width * (640/360)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(360f / 640f) // همونی که تو MediaSlider گذاشتید
            .clip(RoundedCornerShape(30.dp))
    ) {
        ShimmerBox(Modifier.fillMaxSize())
    }
}
