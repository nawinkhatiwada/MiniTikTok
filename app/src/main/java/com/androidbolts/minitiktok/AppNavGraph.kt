package com.androidbolts.minitiktok

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidbolts.minitiktok.features.create.presentation.CreateScreen
import com.androidbolts.minitiktok.features.feed.presentation.FeedScreen
import com.androidbolts.minitiktok.features.feed.presentation.FeedViewModel
import com.androidbolts.minitiktok.features.profile.presentation.ProfileScreen

@Composable
fun AppNavGraph() {
    var selectedTab by remember { mutableStateOf<Screen>(Screen.FeedScreen) }

    // Hoist ViewModel outside the tab switcher so it survives tab changes
    val feedViewModel: FeedViewModel = hiltViewModel()
    val feedUiState by feedViewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                Screen.FeedScreen -> FeedScreen(
                    uiState = feedUiState,
                    onTriggerUserEvent = { feedViewModel.onEvent(it) }
                )
                Screen.CreateScreen -> CreateScreen()
                Screen.ProfileScreen -> ProfileScreen()
            }
        }

        BottomNavBar(
            currentScreen = selectedTab,
            onNavigate = { selectedTab = it }
        )
    }
}
