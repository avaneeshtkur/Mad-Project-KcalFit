package com.kcalfit.app.ui.navigation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kcalfit.app.R
import com.kcalfit.app.ui.reference.*

@Composable
fun BottomNavigationBar(navController: NavController, modifier: Modifier = Modifier) {
    val entry by navController.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    Box(modifier.fillMaxWidth()) {
        Canvas(Modifier.matchParentSize()) {
            val path = Path().apply {
                moveTo(0f, 18.dp.toPx())
                quadraticTo(size.width / 2, -14.dp.toPx(), size.width, 18.dp.toPx())
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(path, ReferenceStyle.Blue)
        }
        Column(Modifier.padding(top = 20.dp).navigationBarsPadding()) {
            if (route == Screen.Home.route) {
                Surface(Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = CircleShape, color = Color.White) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Separate targets keep barcode scanning independent of the search action.
                        Row(
                            Modifier.weight(1f).clickable(role = Role.Button) { navController.navigate(Screen.AddFood.route) }
                                .heightIn(min = 48.dp).padding(start = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Search, null, tint = ReferenceStyle.Blue)
                            Text("Search for food", Modifier.padding(8.dp), color = ReferenceStyle.Muted, fontSize = 14.sp)
                        }
                        IconButton({ navController.navigate(Screen.BarcodeLookup.route) }) {
                            ReferenceIcon(R.drawable.reference_icon_barcode, "Barcode lookup", tint = ReferenceStyle.Blue)
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth().selectableGroup().padding(top = 6.dp, bottom = 8.dp)) {
                listOf(Screen.Home to "Dashboard", Screen.Diary to "Diary", Screen.Profile to "More").forEachIndexed { index, (screen, label) ->
                    val selected = route == screen.route
                    val tint = Color.White.copy(alpha = if (selected) 1f else .72f)
                    Column(
                        Modifier.weight(1f).selectable(selected = selected, role = Role.Tab, onClick = {
                            if (!selected) navController.navigate(screen.route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }).heightIn(min = 64.dp).padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (index == 2) Icon(Icons.Default.Menu, null, tint = tint)
                        else ReferenceIcon(
                            if (index == 0) {
                                if (selected) R.drawable.reference_icon_dashboard else R.drawable.reference_icon_grid
                            } else R.drawable.reference_icon_diary,
                            null, tint = tint
                        )
                        Text(label, fontSize = 12.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = tint, modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
        }
    }
}
