package com.example.solanamobiledappscaffold.presentation.ui.dashboard

import com.example.solanamobiledappscaffold.domain.model.Wallet
import java.math.BigDecimal

data class DashboardState(
    val isLoading: Boolean = false,
    val balance: BigDecimal = BigDecimal(0),
    var wallet: Wallet? = null,
    val error: String = "",
)
