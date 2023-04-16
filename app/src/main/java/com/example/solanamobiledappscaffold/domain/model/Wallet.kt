package com.example.solanamobiledappscaffold.domain.model

import android.net.Uri
import java.math.BigDecimal

data class Wallet(
    val publicKey: String,
    val balance: BigDecimal = BigDecimal(0),
    val walletUriBase: Uri? = null,
    val authToken: String? = null,
)
