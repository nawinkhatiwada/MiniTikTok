package com.androidbolts.minitiktok

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.androidbolts.minitiktok.features.create.presentation.CameraViewModel
import com.androidbolts.minitiktok.features.create.presentation.CreateScreen
import com.androidbolts.minitiktok.features.edit.presentation.EditScreen
import com.androidbolts.minitiktok.features.feed.presentation.FeedScreen
import com.androidbolts.minitiktok.features.feed.presentation.FeedViewModel
import androidx.compose.animation.EnterTransition
import com.androidbolts.minitiktok.features.profile.presentation.ProfileScreen

@Composable
fun AppNavGraph() {
    var selectedTab by remember { mutableStateOf<Screen>(Screen.FeedScreen) }
    var showCreateScreen by remember { mutableStateOf(false) }
    var editUri by remember { mutableStateOf<Uri?>(null) }
    var editIsFrontCamera by remember { mutableStateOf(false) }
    // True when returning from EditScreen — suppresses the slide-up animation
    var createScreenInstantEnter by remember { mutableStateOf(false) }

    // Pre-warm camera provider before user taps Create
    viewModel<CameraViewModel>()

    val feedViewModel: FeedViewModel = viewModel()
    val feedUiState by feedViewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Tab content + persistent bottom nav ───────────────────────────────
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    Screen.FeedScreen -> FeedScreen(
                        uiState = feedUiState,
                        onTriggerUserEvent = { feedViewModel.onEvent(it) },
                        isVisible = !showCreateScreen && editUri == null
                    )

                    Screen.ProfileScreen -> ProfileScreen()
                    Screen.CreateScreen -> { /* handled by full-screen overlay */
                    }
                }
            }

            BottomNavBar(
                currentScreen = selectedTab,
                onNavigate = { screen ->
                    when (screen) {
                        Screen.CreateScreen -> {
                            createScreenInstantEnter = false
                            showCreateScreen = true
                        }

                        else -> selectedTab = screen
                    }
                }
            )
        }

        // ── CreateScreen — slides up from bottom, covers everything ───────────
        // fadeIn/fadeOut intentionally removed: Compose renders the screen into an
        // offscreen buffer when alpha < 1 (graphicsLayer). PlayerView's SurfaceView
        // punches a hole that leads to that buffer → always black. Slide-only
        // transitions use translationY with no offscreen buffer, so video renders.
        AnimatedVisibility(
            visible = showCreateScreen,
            enter = if (createScreenInstantEnter) EnterTransition.None
            else slideInVertically(tween(280, easing = FastOutSlowInEasing)) { it },
            exit = slideOutVertically(
                spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium)
            ) { it }
        ) {
            CreateScreen(
                onClose = { showCreateScreen = false },
                onMediaCaptured = { uri, isFront ->
                    editIsFrontCamera = isFront
                    editUri = uri
                    showCreateScreen = false
                }
            )
        }

        // ── EditScreen — slides up over CreateScreen ───────────────────────────
        AnimatedVisibility(
            visible = editUri != null,
            enter = slideInVertically(tween(280, easing = FastOutSlowInEasing)) { it },
            exit = slideOutVertically(
                spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium)
            ) { it }
        ) {
            val uri = editUri
            if (uri != null) {
                EditScreen(
                    uri = uri,
                    isFrontCamera = editIsFrontCamera,
                    onBack = {
                        createScreenInstantEnter = true
                        editUri = null
                        showCreateScreen = true   // return to camera — no slide-up animation
                    }
                )
            }
        }
    }
}
