package com.michredk.lapselabcompose.ui.startup

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.michredk.lapselabcompose.R
import kotlinx.serialization.Serializable

@Serializable
object SplashScreenDestination

@Composable
fun SplashScreenRoute(navController: NavController) {
    val startupViewModel: StartupViewModel = hiltViewModel()

    SplashScreen(
        navigateAfterAnimation = {
            val nextScreen by startupViewModel.startDestination
            navController.navigate(nextScreen) {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
            }
        }
    )
}

@Composable
fun SplashScreen(navigateAfterAnimation: () -> Unit) {
    val alpha = remember { Animatable(initialValue = 0f) }
    LaunchedEffect(key1 = true) {
        alpha.animateTo(
            1f, animationSpec = tween(
                durationMillis = 1500
            )
        )
        navigateAfterAnimation()
    }

    Column(
        modifier = Modifier
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