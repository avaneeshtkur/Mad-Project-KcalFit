package com.kcalfit.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kcalfit.app.ui.nakafit.Naka

@Composable
fun BottomNavigationBar(navController: NavController, modifier: Modifier = Modifier) {
    val entry by navController.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    Box(
        modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(top = 8.dp, bottom = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(Modifier.widthIn(max = 300.dp).background(Naka.Ink, RoundedCornerShape(40.dp)).padding(7.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Screen.bottomNavItems.forEach { screen ->
                IconButton(onClick = {
                    if (route != screen.route) navController.navigate(screen.route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }, modifier = Modifier.size(48.dp).background(if (route == screen.route) Naka.Violet else Color.Transparent, CircleShape)) {
                    Icon(screen.icon, screen.title, Modifier.size(22.dp), tint = if (route == screen.route) Color.White else Naka.Muted)
                }
            }
        }
    }
}
