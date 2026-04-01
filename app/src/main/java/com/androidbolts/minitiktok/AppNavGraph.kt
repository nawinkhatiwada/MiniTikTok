package com.androidbolts.minitiktok

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.androidbolts.minitiktok.features.feed.presentation.FeedScreen
import com.androidbolts.minitiktok.features.feed.presentation.FeedViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.FeedScreen
    ) {

        composable<Screen.FeedScreen> {
            val viewModel: FeedViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            FeedScreen(
                uiState = uiState,
                onTriggerUserEvent = {
                    viewModel.onEvent(it)
                }
            )
        }

    }
}