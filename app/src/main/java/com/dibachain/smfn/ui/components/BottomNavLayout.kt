// جایی مثل ui/components/BottomNavLayout.kt
package com.dibachain.smfn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navOptions
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.dibachain.smfn.R

data class BottomTab(
    val route: String,
    val item: BottomItem
)

@Composable
fun BottomNavLayout(
    nav: NavController,
    tabs: List<BottomTab>,
    modifier: Modifier = Modifier,
    barPadding: PaddingValues = PaddingValues(start = 20.dp, end = 20.dp, bottom = 26.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val selectedIndex = remember(currentRoute, tabs) {
        tabs.indexOfFirst { r ->
            // اگر گراف تب‌ها زیرگراف داشته باشه، startsWith کمک می‌کنه
            currentRoute?.startsWith(r.route) == true
        }.let { if (it == -1) 0 else it }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F5F8)) // پس‌زمینه 默
    ) {
        // محتوای صفحه
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            content()
        }

        // نوار پایین
        GradientBottomBar(
            items = tabs.map { it.item },
            selectedIndex = selectedIndex,
            onSelect = { idx ->
                val route = tabs[idx].route
                if (currentRoute != route) {
                    nav.navigate(route, navOptions {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(nav.graph.findStartDestination().id) {
                            saveState = true
                        }
                    })
                }
            },
            modifier = Modifier.padding(barPadding)
        )
    }
}
