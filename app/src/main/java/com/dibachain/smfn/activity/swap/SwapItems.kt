package com.dibachain.smfn.activity.swap

import androidx.compose.ui.graphics.painter.Painter

data class SwapUser(
    val avatar: Painter,
    val name: String,
    val location: String
)

data class SwapItem(
    val image: Painter
)