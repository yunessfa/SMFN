package com.dibachain.smfn.activity.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dibachain.smfn.R

/* ---------- مدل‌ها ---------- */
enum class Relation {
    NotFollowing,      // شما هنوز فالو نکردید -> Follow (مشکی)
    FollowsYou,        // طرف شما را فالو کرده -> Follow back (مشکی)
    FollowingActive,   // شما فالو هستید -> Following (مشکی)
    FollowingDisabled  // شما فالو هستید ولی دکمه غیرفعال خاکستری (مطابق یکی از طرح‌ها)
}

data class FollowUserUi(
    val id: String,
    val name: String,
    val avatar: String,
    val relation: Relation
)

/* ---------- صفحه ---------- */
@Composable
fun FollowersFollowingScreen(
    titleUserName: String,
    followersCount: Int,
    loadingUserIds: Set<String>, // ← جدید
    followingCount: Int,
    followers: List<FollowUserUi>,
    following: List<FollowUserUi>,
    onBack: () -> Unit,
    onOpenProfile: (FollowUserUi) -> Unit = {},
    onFollowAction: (FollowUserUi, Relation) -> Unit = { _, _ -> },
) {
    var tab by remember { mutableStateOf(0) } // 0: Followers, 1: Following
    var query by remember { mutableStateOf(TextFieldValue("")) }

    val list = remember(tab, followers, following, query.text) {
        val src = if (tab == 0) followers else following
        if (query.text.isBlank()) src else src.filter { it.name.contains(query.text, true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)       // ← سفیدِ همیشگی
            .statusBarsPadding()           // ← فقط همین یک‌بار
            .padding(horizontal = 16.dp)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painterResource(R.drawable.ic_swap_back),
                    contentDescription = "Back",
                    tint = Color(0xFF292D32),
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                titleUserName,
                style = TextStyle(
                    fontSize = 24.sp,
                    lineHeight = 33.3.sp,
                    fontFamily = FontFamily(Font(R.font.inter_regular)),
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF292D32),
                )
            )
            Box(modifier = Modifier.size(36.dp))
        }

        // Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            TabCount("${followersCount} Follower", selected = tab == 0) { tab = 0 }
            TabCount("${followingCount} Following", selected = tab == 1) { tab = 1 }
        }

        Spacer(Modifier.height(16.dp))

        // Search
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            leadingIcon = {
                Icon(
                    painterResource(R.drawable.ic_search),
                    contentDescription = null,
                    tint = Color(0xFFA0A0A0),
                    modifier = Modifier.size(19.dp)
                )
            },
            placeholder = {
                Text(
                    "Search",
                    style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 19.6.sp,
                        fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFFA0A0A0),
                    )
                )
            },
            textStyle = TextStyle(
                fontSize = 14.sp,
                lineHeight = 19.6.sp,
                fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                fontWeight = FontWeight.Normal,
                color = Color(0xFF000000),
            ),
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF2F2F2),
                unfocusedContainerColor = Color(0xFFF2F2F2),
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = Color(0xFF000000)
            )
        )

        Spacer(Modifier.height(12.dp))

        // List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(list, key = { it.id }) { u ->
                val isLoading = loadingUserIds.contains(u.id)
                UserRow(
                    user = u,
                    isLoading = isLoading,                       // ← جدید
                    onClick = { onOpenProfile(u) },
                    onButtonClick = { next -> onFollowAction(u, next) }
                )
            }
            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

/* ---------- اجزای کمکی ---------- */

@Composable
private fun RowScope.TabCount(text: String, selected: Boolean, onClick: () -> Unit) {
    val underline = if (selected) Color(0xFFE0E0E0) else Color.Transparent
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text,
            style = TextStyle(
                fontSize = 22.sp,
                lineHeight = 33.3.sp,
                fontFamily = FontFamily(Font(R.font.inter_regular)),
                fontWeight = FontWeight(400),
                color = Color(0xFF292D32),
                )
        )
        Spacer(
            Modifier
                .padding(top = 6.dp)
                .height(2.dp)
                .fillMaxWidth()
                .background(underline, RoundedCornerShape(2.dp))
        )
    }
}

@Composable
private fun UserRow(
    user: FollowUserUi,
    isLoading: Boolean, // ← جدید

    onClick: () -> Unit,
    onButtonClick: (nextRelation: Relation) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = user.avatar,
            contentDescription = null,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFFEDEDED))
        )
        Spacer(Modifier.width(12.dp))
        Text(
            user.name,
            modifier = Modifier.weight(1f),
            style = TextStyle(
                fontSize = 14.98.sp,
                fontFamily = FontFamily(Font(R.font.latob_bold)),
                fontWeight = FontWeight(700),
                color = Color(0xFF252525),
                )
        )

        FollowPill(
            relation = user.relation,
            loading = isLoading,              // ← جدید
            onClick = {
                // منطق ساده: بین حالت‌ها سوئیچ کن و به بیرون اطلاع بده
                val next = when (user.relation) {
                    Relation.NotFollowing   -> Relation.FollowingActive
                    Relation.FollowsYou     -> Relation.FollowingActive
                    Relation.FollowingActive -> Relation.NotFollowing
                    Relation.FollowingDisabled -> Relation.FollowingDisabled // دکمه غیرفعال
                }
                onButtonClick(next)
            }
        )
    }
}

@Composable
private fun FollowPill(
    relation: Relation,
    loading: Boolean, // ← جدید
    onClick: () -> Unit
) {
    val (text, container, content, enabled) = when (relation) {
        Relation.NotFollowing -> Quad("Follow",       Color(0xFF000000), Color.White, true)
        Relation.FollowsYou ->   Quad("Follow back",  Color(0xFF000000), Color.White, true)
        Relation.FollowingActive -> Quad("Following", Color(0xFF000000), Color.White, true)
        Relation.FollowingDisabled -> Quad("Following", Color(0xFF797B82), Color.White, false)
    }

    val shape = RoundedCornerShape(24.dp)
    val effectiveEnabled = enabled && !loading

    if (effectiveEnabled) {
        Surface(onClick = onClick, shape = shape, color = container, modifier = Modifier.height(40.dp)) {
            Box(Modifier.width(132.dp).height(38.dp), contentAlignment = Alignment.Center) {
                if (loading) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp), color = content)
                } else {
                    Text(text, style = TextStyle(fontSize = 12.35.sp, fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                        fontWeight = FontWeight(500), color = content))
                }
            }
        }
    } else {
        Box(
            modifier = Modifier.width(132.dp).height(38.dp).clip(shape).background(container),
            contentAlignment = Alignment.Center
        ) {
            if (loading) {
                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp), color = content)
            } else {
                Text(text, style = TextStyle(fontSize = 12.35.sp, fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                    fontWeight = FontWeight(500), color = content))
            }
        }
    }
}

 data class Quad<A,B,C,D>(val a:A,val b:B,val c:C,val d:D)
 operator fun <A,B,C,D> Quad<A,B,C,D>.component1()=a
 operator fun <A,B,C,D> Quad<A,B,C,D>.component2()=b
 operator fun <A,B,C,D> Quad<A,B,C,D>.component3()=c
 operator fun <A,B,C,D> Quad<A,B,C,D>.component4()=d
/* ---------- پیش‌نمایش ---------- */
//@Composable
//private fun demoUsers() = listOf(
//    FollowUserUi("1","Jacob Jones","https://i.pravatar.cc/150?img=1", Relation.FollowsYou),
//    FollowUserUi("2","Jacob Jones","https://i.pravatar.cc/150?img=5", Relation.NotFollowing),
//    FollowUserUi("3","Jacob Jones","https://i.pravatar.cc/150?img=7", Relation.FollowingDisabled),
//)
//
//@Composable
//private fun demoUsersFollowing() = listOf(
//    FollowUserUi("4","Jacob Jones","https://i.pravatar.cc/150?img=12", Relation.FollowingActive),
//    FollowUserUi("5","Jacob Jones","https://i.pravatar.cc/150?img=13", Relation.FollowingActive),
//    FollowUserUi("6","Jacob Jones","https://i.pravatar.cc/150?img=14", Relation.FollowingActive),
//    FollowUserUi("7","Jacob Jones","https://i.pravatar.cc/150?img=1", Relation.FollowsYou),
//    FollowUserUi("8","Jacob Jones","https://i.pravatar.cc/150?img=7", Relation.FollowingDisabled),
//)
//
//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
//@Composable
//private fun Followers_Preview() {
//    FollowersFollowingScreen(
//        titleUserName = "Jolie",
//        followersCount = 3,
//        followingCount = 4,
//        followers = demoUsers(),
//        following = demoUsersFollowing(),
//        onBack = {},
//        onFollowAction = { _, _ -> }
//    )
//}
