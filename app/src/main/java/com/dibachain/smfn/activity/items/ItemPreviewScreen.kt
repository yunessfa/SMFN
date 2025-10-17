// com.dibachain.smfn.activity.items.ItemPublishPreviewScreen.kt
package com.dibachain.smfn.activity.items

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.dibachain.smfn.R
import com.dibachain.smfn.activity.feature.product.ProductPayload

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ItemPublishPreviewScreen(
    payload: ProductPayload,
    onBack: () -> Unit,
    onPublish: () -> Unit,
    onEdit: () -> Unit
) {
    val listState = rememberLazyListState()
    val images = remember(payload.cover, payload.photos) { listOf(payload.cover) + payload.photos }
    val painters = images.map { rememberAsyncImagePainter(it) }

    val conditionSub = when (payload.condition) {
        "Brand new" -> "Never used, sealed, or freshly unboxed."
        "Like new"  -> "Lightly used and fully functional, with no signs of usage."
        "Good"      -> "Gently used and may have minor cosmetic flaws, fully functional."
        "Fair"      -> "Used and has multiple cosmetic flaw,but over all functional"
        else        -> ""
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(Color(0xFFF8F8F8))
    ) {
        item {
            DetailHeaderSlider(
                images = painters,
                likeCount = 0,
                isFavorite = false,
                backIcon = painterResource(R.drawable.ic_items_back),
                shareIcon = painterResource(R.drawable.ic_upload_items),
                moreIcon = painterResource(R.drawable.ic_menu_revert),
                starIcon = painterResource(R.drawable.ic_menu_agenda),
                onBack = onBack,
                onShare = {},
                onMore = {},
                onToggleFavorite = {}
            )
        }
        item {
            ItemDetailContentNoTabs(
                title = payload.name,
                description = payload.description,
                conditionTitle = payload.condition,
                conditionSub = conditionSub,
                valueText = "AED ${payload.valueAed}",
                categories = payload.categories.map { it.replaceFirstChar { c -> c.uppercase() } },
                location = payload.location,
                uploadedAt = java.time.LocalDate.now().toString()
            )
        }
        item { Spacer(Modifier.height(18.dp)) }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFFFFD25A), Color(0xFF42C695))))
                        .clickable { onPublish() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Publish Item", color = Color.White, style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(10.dp))
                TextButton(
                    onClick = onEdit,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) { Text("Edit") }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun ItemDetailContentNoTabs(
    title: String,
    description: String,
    conditionTitle: String,
    conditionSub: String,
    valueText: String,
    categories: List<String>,
    location: String,
    uploadedAt: String
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium.copy(color = Color(0xFF292D32)))
        Spacer(Modifier.height(18.dp))
        SectionTitle("Item Description")
        BodyText(description)
        Spacer(Modifier.height(18.dp))
        SectionTitle("Item Condation") // مطابق طرح
        Text(conditionTitle, style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF292D32)))
        Spacer(Modifier.height(6.dp))
        BodyText(conditionSub)
        Spacer(Modifier.height(18.dp))
        SectionTitle("Value")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(R.drawable.ic_camera_items), contentDescription = null, tint = Color(0xFF292D32))
            Spacer(Modifier.width(6.dp))
            Text(valueText, style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF292D32)))
        }
        Spacer(Modifier.height(18.dp))
        SectionTitle("Category")
        FlowChips(items = categories)
        Spacer(Modifier.height(18.dp))
        SectionTitle("Location")
        Row(verticalAlignment = Alignment.CenterVertically) {
//            Icon(painterResource(R.drawable.ic_location), contentDescription = null, tint = Color(0xFF292D32))
//            Spacer(Modifier.width(6.dp))
            Column {
                Text(location, style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF292D32)))
                Text("(0)Km from you", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFAAAAAA)))
            }
        }
        Spacer(Modifier.height(18.dp))
        SectionTitle("Uploaded at")
        Text(uploadedAt, style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF292D32)))
    }
}

//@RequiresApi(Build.VERSION_CODES.O)
//@Preview(showBackground = true, backgroundColor = 0xFFF8F8F8)
//@Composable
//private fun ItemPublishPreviewScreen_Preview() {
//    val fake = ProductPayload(
//        categories = setOf("Music","Drink"),
//        name = "Canon4000D",
//        description = "xvbn",
//        condition = "Fair",
//        photos = listOf("https://picsum.photos/seed/1/600/400"),
//        cover = "https://images.pexels.com/photos/167832/pexels-photo-167832.jpeg",
//        video = "preview://video",
//        tags = listOf(),
//        valueAed = 4501,
//        location = "Garden City"
//    )
//    ItemPublishPreviewScreen(
//        payload = fake,
//        onBack = {},
//        onPublish = {},
//        onEdit = {}
//    )
//}
