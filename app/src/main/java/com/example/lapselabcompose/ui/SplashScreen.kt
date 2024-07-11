package com.example.lapselabcompose.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.lapselabcompose.ui.theme.LapseLabComposeTheme
import com.example.lapselabcompose.R
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(navController: NavController) {
    val alpha = remember { Animatable(initialValue = 0f) }
    LaunchedEffect(key1 = true) {
        launch {
            alpha.animateTo(
                1f, animationSpec = tween(
                    durationMillis = 1500
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
                .background(MaterialTheme.colorScheme.background)
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