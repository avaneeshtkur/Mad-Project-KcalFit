package com.kcalfit.app.ui.components
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kcalfit.app.R
@Composable fun CalFitLogo(size:Dp=48.dp,showText:Boolean=true){Row(verticalAlignment=Alignment.CenterVertically){Icon(painterResource(R.drawable.ic_calfit_logo),"CalFit Logo",Modifier.size(size),tint=MaterialTheme.colorScheme.primary);if(showText){Spacer(Modifier.width(8.dp));Text("Cal",style=MaterialTheme.typography.titleLarge,color=MaterialTheme.colorScheme.primary);Text("Fit",style=MaterialTheme.typography.titleLarge,color=MaterialTheme.colorScheme.tertiary)}}}
