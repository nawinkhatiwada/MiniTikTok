package com.androidbolts.minitiktok.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidbolts.minitiktok.core.utils.AppConstants.EMPTY_STRING
import com.androidbolts.minitiktok.core.utils.ResultType
import com.androidbolts.minitiktok.features.auth.domain.model.User
import com.androidbolts.minitiktok.features.auth.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    private fun updateUiState(block: (LoginUiState) -> LoginUiState) {
        _uiState.value = block(_uiState.value)
    }

    fun onEvent(event: LoginUserEvent) {
        when (event) {
            is LoginUserEvent.OnEmailChanged -> {
                updateUiState { it.copy(email = event.email) }
            }

            is LoginUserEvent.OnPasswordChanged -> {
                updateUiState { it.copy(password = event.password) }
            }

            is LoginUserEvent.OnLoginClicked -> {
                login()
            }
        }
    }

    fun login() {
        if (uiState.value.isLoading) return
        updateUiState { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = loginUseCase(
                email = uiState.value.email,
                password = uiState.value.password
            )) {
                is ResultType.Success -> {
                    updateUiState {
                        it.copy(isLoading = false, user = result.data)
                    }
                }

                is ResultType.Error -> {
                    updateUiState {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
            }
        }
    }
}

data class LoginUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val email: String = EMPTY_STRING,
    val password: String = EMPTY_STRING
)

sealed class LoginUserEvent {
    data class OnEmailChanged(val email: String) : LoginUserEvent()
    data class OnPasswordChanged(val password: String) : LoginUserEvent()
    object OnLoginClicked : LoginUserEvent()
}
