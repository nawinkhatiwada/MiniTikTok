package com.androidbolts.minitiktok.features.feed.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidbolts.minitiktok.core.common.enums.FeedType
import com.androidbolts.minitiktok.core.utils.AppConstants.EMPTY_STRING
import com.androidbolts.minitiktok.core.utils.ResultType
import com.androidbolts.minitiktok.features.feed.domain.model.Feed
import com.androidbolts.minitiktok.features.feed.domain.usecase.FeedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val getFeedUseCase: FeedUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState

    private fun updateUiState(block: (FeedUiState) -> FeedUiState) {
        _uiState.value = block(_uiState.value)
    }

    private var currentPage = 1
    private var isLoadingMore = false
    private var hasReachedEnd = false

    init {
        loadFeed()
    }

    private fun loadFeed() {
        updateUiState {
            it.copy(
                isLoading = true,
                errorMessage = EMPTY_STRING
            )
        }

        viewModelScope.launch {
            fetchFeedData()
        }
    }

    private fun onFeedTypeChanged(feedType: FeedType) {
        if (uiState.value.feedType == feedType) return
        currentPage = 1
        hasReachedEnd = false

        updateUiState {
            it.copy(
                feedType = feedType,
                videos = emptyList(),
                isLoading = true,
                errorMessage = EMPTY_STRING
            )
        }

        viewModelScope.launch {
            fetchFeedData()
        }
    }

    private suspend fun fetchFeedData() {
        val type = uiState.value.feedType

        when (val result = getFeedUseCase(currentPage, type)) {

            is ResultType.Success -> {
                updateUiState {
                    it.copy(
                        videos = result.data,
                        isLoading = false
                    )
                }
            }

            is ResultType.Error -> {
                updateUiState {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    private fun loadMoreFeed() {
        if (isLoadingMore || hasReachedEnd) return

        val type = uiState.value.feedType
        val nextPage = currentPage + 1

        isLoadingMore = true
        updateUiState { it.copy(isLoadingMore = true) }

        viewModelScope.launch {
            when (val result = getFeedUseCase(nextPage, type)) {
                is ResultType.Success -> {
                    if (result.data.isEmpty()) {
                        hasReachedEnd = true
                    } else {
                        currentPage = nextPage
                    }

                    updateUiState {
                        it.copy(
                            videos = it.videos + result.data,
                            isLoadingMore = false
                        )
                    }
                }

                is ResultType.Error -> {
                    updateUiState {
                        it.copy(
                            errorMessage = result.message,
                            isLoadingMore = false
                        )
                    }
                }
            }

            isLoadingMore = false
        }
    }

    fun onEvent(event: FeedUserEvent) {
        when (event) {
            is FeedUserEvent.OnFeedTypeChanged -> onFeedTypeChanged(event.feedType)
            is FeedUserEvent.OnLoadMoreFeed -> loadMoreFeed()
            // TODO remove else and handle other
            else -> {}
        }

    }
}

data class FeedUiState(
    val videos: List<Feed> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String = EMPTY_STRING,
    val isLoadingMore: Boolean = false,
    val feedType: FeedType = FeedType.FOR_YOU
)

sealed class FeedUserEvent {
    data class OnFeedTypeChanged(val feedType: FeedType) : FeedUserEvent()
    data object OnLoadMoreFeed: FeedUserEvent()
    data class OnLikeClicked(val videoId: String) : FeedUserEvent()
    data class OnCommentClicked(val videoId: String) : FeedUserEvent()
    data class OnShareClicked(val videoId: String): FeedUserEvent()
}