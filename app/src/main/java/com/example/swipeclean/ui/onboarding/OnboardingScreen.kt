package com.example.swipeclean.ui.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.swipeclean.R
import com.example.swipeclean.haptics.rememberTapHaptic
import kotlinx.coroutines.launch

private enum class OnboardingStep { Pager, Permissions }

@Composable
fun OnboardingRoute(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.complete && !state.loading) {
        onFinished()
        return
    }

    OnboardingScreen(
        onFullyFinished = {
            viewModel.complete()
            onFinished()
        }
    )
}

@Composable
private fun OnboardingScreen(
    onFullyFinished: () -> Unit
) {
    var step by remember { mutableStateOf(OnboardingStep.Pager) }
    val tap = rememberTapHaptic()

    when (step) {
        OnboardingStep.Permissions -> OnboardingPermissionFinale(
            onContinueToApp = onFullyFinished,
            onBackToIntro = {
                tap()
                step = OnboardingStep.Pager
            }
        )
        OnboardingStep.Pager -> OnboardingPager(
            onSkipOrFinishIntro = {
                tap()
                step = OnboardingStep.Permissions
            }
        )
    }
}

@Composable
private fun OnboardingPager(
    onSkipOrFinishIntro: () -> Unit
) {
    val pages = rememberOnboardingPages()
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val tap = rememberTapHaptic()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 22.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        tap()
                        onSkipOrFinishIntro()
                    }
                ) {
                    Text(stringResource(R.string.onboarding_skip))
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                pageSpacing = 16.dp
            ) { pageIndex ->
                OnboardingPage(page = pages[pageIndex])
            }

            PagerIndicators(
                pageCount = pages.size,
                currentPage = pagerState.currentPage,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )

            val isLast = pagerState.currentPage == pages.lastIndex
            Button(
                onClick = {
                    tap()
                    if (isLast) {
                        onSkipOrFinishIntro()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                pagerState.currentPage + 1,
                                animationSpec = spring()
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.extraLarge,
                contentPadding = PaddingValues(horizontal = 24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = if (isLast) {
                        stringResource(R.string.onboarding_get_started)
                    } else {
                        stringResource(R.string.onboarding_next)
                    },
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun OnboardingPage(page: OnboardingPageContent) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            page.illustration(Modifier.fillMaxSize())
        }
        Text(
            text = stringResource(page.titleRes),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = stringResource(page.bodyRes),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun PagerIndicators(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val active = index == currentPage
            val width by animateDpAsState(if (active) 26.dp else 7.dp, label = "indicator-width")
            val color by animateColorAsState(
                if (active) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
                label = "indicator-color"
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .height(7.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

private data class OnboardingPageContent(
    val titleRes: Int,
    val bodyRes: Int,
    val illustration: @Composable (Modifier) -> Unit
)

@Composable
private fun rememberOnboardingPages(): List<OnboardingPageContent> = remember {
    listOf(
        OnboardingPageContent(
            titleRes = R.string.onboarding_p1_title,
            bodyRes = R.string.onboarding_p1_body,
            illustration = { mod -> SwipeIllustration(mod) }
        ),
        OnboardingPageContent(
            titleRes = R.string.onboarding_p2_title,
            bodyRes = R.string.onboarding_p2_body,
            illustration = { mod -> BinIllustration(mod) }
        ),
        OnboardingPageContent(
            titleRes = R.string.onboarding_p3_title,
            bodyRes = R.string.onboarding_p3_body,
            illustration = { mod -> ProgressIllustration(mod) }
        ),
        OnboardingPageContent(
            titleRes = R.string.onboarding_p4_title,
            bodyRes = R.string.onboarding_p4_body,
            illustration = { mod -> StarIllustration(mod) }
        )
    )
}
