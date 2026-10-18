package com.kcalfit.app.ui.splash
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.kcalfit.app.data.model.UserEntity
import com.kcalfit.app.ui.navigation.Screen
import com.kcalfit.app.ui.reference.*
@Composable fun SplashScreen(userProfile: UserEntity?, authReady: Boolean, onNavigateNext: (String)->Unit, modifier: Modifier=Modifier) {
    LaunchedEffect(authReady,userProfile) { if(authReady) { kotlinx.coroutines.delay(350); onNavigateNext(when {userProfile==null || !userProfile.isLoggedIn->Screen.Welcome.route;!userProfile.isOnboarded->Screen.Onboarding.route;else->Screen.Home.route}) } }
    Box(modifier.fillMaxSize().background(ReferenceStyle.Blue),contentAlignment=Alignment.Center) {KcalfitWordmark(color=Color.White,size=36.sp)}
}
