package com.kcalfit.app.ui.theme
import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
private val DarkScheme=darkColorScheme(primary=DarkPrimary,onPrimary=DarkOnPrimary,primaryContainer=DarkPrimaryContainer,onPrimaryContainer=DarkOnPrimaryContainer,secondary=DarkSecondary,secondaryContainer=DarkSecondaryContainer,onSecondaryContainer=DarkOnSecondaryContainer,tertiary=DarkTertiary,tertiaryContainer=DarkTertiaryContainer,background=DarkBackground,surface=DarkSurface,surfaceVariant=DarkSurfaceVariant,onSurface=DarkOnSurface,onSurfaceVariant=DarkOnSurfaceVariant,outline=DarkOutline,error=DarkError)
private val LightScheme=lightColorScheme(primary=LightPrimary,onPrimary=LightOnPrimary,primaryContainer=LightPrimaryContainer,onPrimaryContainer=LightOnPrimaryContainer,secondary=LightSecondary,secondaryContainer=LightSecondaryContainer,onSecondaryContainer=LightOnSecondaryContainer,tertiary=LightTertiary,tertiaryContainer=LightTertiaryContainer,background=LightBackground,surface=LightSurface,surfaceVariant=LightSurfaceVariant,onSurface=LightOnSurface,onSurfaceVariant=LightOnSurfaceVariant,outline=LightOutline,error=LightError)
@Composable fun CalFitTheme(darkTheme:Boolean=isSystemInDarkTheme(), content: @Composable () -> Unit){val scheme=if(darkTheme)DarkScheme else LightScheme;val view=LocalView.current;if(!view.isInEditMode)SideEffect{val w=(view.context as Activity).window;w.statusBarColor=scheme.background.toArgb();w.navigationBarColor=scheme.surface.toArgb();WindowCompat.getInsetsController(w,view).isAppearanceLightStatusBars=!darkTheme;WindowCompat.getInsetsController(w,view).isAppearanceLightNavigationBars=!darkTheme};MaterialTheme(colorScheme=scheme,typography=CalFitTypography,content=content)}
