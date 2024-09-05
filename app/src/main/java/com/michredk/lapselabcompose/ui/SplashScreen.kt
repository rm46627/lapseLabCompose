package com.michredk.lapselabcompose.ui

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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.michredk.database.DataStoreRepository
import com.michredk.lapselabcompose.R
import com.michredk.lapselabcompose.ui.gallery.GalleryDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
object SplashScreenDestination

@Composable
fun SplashScreenRoute(navController: NavController) {
    val splashViewModel: SplashViewModel = hiltViewModel()

    SplashScreen(
        navigateAfterAnimation = {
            val nextScreen by splashViewModel.startDestination
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

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val repository: DataStoreRepository
) : ViewModel() {

    private val _isLoading: MutableState<Boolean> = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _startDestination: MutableState<Any> = mutableStateOf(SplashScreenDestination)
    val startDestination: State<Any> = _startDestination

    init {
        viewModelScope.launch {
            repository.readOnBoardingState().collect { completed ->
                if (completed) {
                    _startDestination.value = GalleryDestination
                } else {
                    _startDestination.value = OnBoardingDestination
                }
            }
            _isLoading.value = false
        }
    }

    fun saveOnBoardingState(completed: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveOnBoardingState(completed = completed)
        }
    }

}