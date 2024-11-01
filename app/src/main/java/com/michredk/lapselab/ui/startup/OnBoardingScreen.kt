package com.michredk.lapselab.ui.startup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.michredk.lapselab.R
import com.michredk.lapselab.ui.common.GifImage
import com.michredk.lapselab.ui.gallery.GalleryDestination
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.math.absoluteValue

@Serializable
object OnBoardingDestination

@Composable
fun OnBoardingRoute(navController: NavController) {

    val startupViewModel: StartupViewModel = hiltViewModel()

    OnBoardingScreen(
        onProceedClicked = {
            startupViewModel.saveOnBoardingState(true)
            navController.navigate(GalleryDestination) {
                popUpTo(OnBoardingDestination) {
                    inclusive = true
                }
            }
        }
    )
}

@Composable
fun OnBoardingScreen(onProceedClicked: () -> Unit) {
    Box() {

    }
    Column(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .weight(4f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val pagerState = rememberPagerState(pageCount = {
                3
            })
            val coroutineScope = rememberCoroutineScope()
            HorizontalPager(
                state = pagerState,
                pageSpacing = 12.dp,
                contentPadding = PaddingValues(
                    horizontal = 32.dp,
                    vertical = 8.dp
                )
            ) { page ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(700.dp)
                        .graphicsLayer {
                            // Calculate the absolute offset for the current page from the
                            // scroll position. We use the absolute value which allows us to mirror
                            // any effects for both directions
                            val pageOffset = (
                                    (pagerState.currentPage - page) + pagerState
                                        .currentPageOffsetFraction
                                    ).absoluteValue
                            // We animate the alpha, between 50% and 100%
                            alpha = lerp(
                                start = 0.5f,
                                stop = 1f,
                                fraction = 1f - pageOffset.coerceIn(0f, 1f)
                            )
                        }
                ) {
                    // Card content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val offset = 400f
                        val titleBrush = Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.onPrimaryContainer,
                                MaterialTheme.colorScheme.primary
                            ),
                            tileMode = TileMode.Mirror,
                            start = Offset(0f, 0f),
                            end = Offset(offset, offset)
                        )
                        if (page == 0) {
                            Text(
                                textAlign = TextAlign.Center, style = TextStyle(
                                    brush = titleBrush,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = MaterialTheme.typography.headlineLarge.fontSize
                                ), text = stringResource(R.string.create_albums_and_add_photos)
                            )
                            GifImage(
                                data = R.drawable.albums, modifier = Modifier
                                    .padding(top = 16.dp)
                                    .height(400.dp)
                                    .fillMaxWidth()
                            )
                            OutlinedButton(onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(1)
                                }
                            }) {
                                Text(text = stringResource(R.string.next))
                            }
                        }
                        if (page == 1) {
                            Text(
                                textAlign = TextAlign.Center, style = TextStyle(
                                    brush = titleBrush,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = MaterialTheme.typography.headlineLarge.fontSize
                                ), text = stringResource(R.string.then_make_unforgettable_memories)
                            )
                            GifImage(
                                data = R.drawable.oliwa, modifier = Modifier
                                    .padding(top = 16.dp)
                                    .height(175.dp)
                                    .fillMaxWidth()
                            )
                            GifImage(
                                data = R.drawable.city, modifier = Modifier
                                    .padding(top = 16.dp)
                                    .height(175.dp)
                                    .fillMaxWidth()
                            )
                            OutlinedButton(onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(2)
                                }
                            }) {
                                Text(text = stringResource(R.string.next))
                            }
                        }
                        if (page == 2) {
                            Text(
                                textAlign = TextAlign.Center,
                                style = TextStyle(
                                    brush = titleBrush,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = MaterialTheme.typography.headlineLarge.fontSize
                                ),
                                text = stringResource(R.string.or_just_have_fun_creating_silly_videos)
                            )
                            GifImage(
                                data = R.drawable.dragon, modifier = Modifier
                                    .padding(top = 16.dp, bottom = 8.dp)
                                    .height(400.dp)
                                    .fillMaxWidth()
                            )
                            OutlinedButton(onClick = onProceedClicked) {
                                Text(text = stringResource(R.string.proceed))
                            }
                        }

                    }
                }
            }
        }
//        Spacer(modifier = Modifier.weight(1f))
    }
}