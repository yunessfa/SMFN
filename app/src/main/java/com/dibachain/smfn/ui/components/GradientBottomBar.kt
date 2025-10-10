package com.dibachain.smfn.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val BarBg = Color(0xFF2F2D30)
private val ActiveGradient = Brush.linearGradient(
    listOf(Color(0xFFFFC753), Color(0xFF4AC0A8))
)

data class BottomItem(
    val id: String,
    val activePainter: Painter,   // outline (selected)
    val inactivePainter: Painter  // bold (unselected)
)

@Composable
fun GradientBottomBar(
    items: List<BottomItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    activeCircleSize: Dp = 59.dp,
    iconSize: Dp = 31.dp,
    sidePadding: Dp = 6.dp,               // ← پدینگ افقی و عمودی یکی
) {
    val barShape = RoundedCornerShape(50.dp)
    val barHeight = activeCircleSize + sidePadding * 2

    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(barHeight)
                .clip(barShape)
                .background(BarBg)
                .padding(horizontal = sidePadding, vertical = sidePadding)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    BottomBarButton(
                        item = item,
                        active = index == selectedIndex,
                        activeCircleSize = activeCircleSize,
                        iconSize = iconSize,
                        onClick = { onSelect(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomBarButton(
    item: BottomItem,
    active: Boolean,
    activeCircleSize: Dp,
    iconSize: Dp,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }

    // عرض معقول برای تاچ؛ ارتفاع از والد می‌آد (71dp)
    Box(
        modifier = Modifier
            .width(72.dp)
            .fillMaxHeight()
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (active) {
            // ✅ دایره‌ی واقعی: اندازه قفل + بک‌گراند با shape دایره
            Box(
                modifier = Modifier
                    .requiredSize(activeCircleSize)
                    .background(ActiveGradient, shape = CircleShape)
            )
        }
        Image(
            painter = if (active) item.activePainter else item.inactivePainter,
            contentDescription = item.id,
            modifier = Modifier.requiredSize(iconSize)
        )
    }
}
