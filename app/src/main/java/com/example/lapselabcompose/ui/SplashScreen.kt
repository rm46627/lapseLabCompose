package com.example.lapselabcompose.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.compose.LapseLabComposeTheme
import com.example.compose.primaryLight
import com.example.lapselabcompose.R
import kotlinx.coroutines.launch


//val scaleA = remember { Animatable(initialValue = 1f) }
//val scaleB = remember { Animatable(initialValue = 1f) }
//
//val clickEnabled = remember { mutableStateOf(true) }
//
//LaunchedEffect(key1 = selected) {
//    if (selected) {
//        clickEnabled.value = false
//
//        val jobA = launch {
//            scaleA.animateTo(
//                targetValue = 0.3f,
//                animationSpec = tween(
//                    durationMillis = 50
//                )

@Composable
fun SplashScreen(navController: NavController) {

    val alpha = remember { Animatable(initialValue = 0f) }
    LaunchedEffect(key1 = true) {
        launch {
            alpha.animateTo(
                1f,
                animationSpec = tween(
                    durationMillis = 500
                )
            )
            alpha.animateTo(
                0f,
                animationSpec = tween(
                    durationMillis = 500
                )
            )
            
            navController.navigate(GalleryScreen)
        }
    }

    Scaffold {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .background(primaryLight)
        ) {
            Icon(
                modifier = Modifier
                    .weight(3f)
                    .scale(0.95f)
                    .alpha(alpha.value),
                tint = Color.Unspecified,
                painter = painterResource(id = R.drawable.logofinal),
                contentDescription = "LapseLab logo"
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Preview
@Composable
fun PreviewSplash() {
    LapseLabComposeTheme {
        SplashScreen(rememberNavController())
    }
}