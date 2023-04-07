package com.example.solanamobiledappscaffold.presentation.ui.dashboard

import androidx.lifecycle.ViewModel
import com.example.solanamobiledappscaffold.domain.use_case.authorize_wallet.AuthorizeWalletUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authorizeWalletUseCase: AuthorizeWalletUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardState())
    val uiState = _uiState.asStateFlow()

    companion object {
        private const val TAG = "DashboardViewModel"
    }
}
