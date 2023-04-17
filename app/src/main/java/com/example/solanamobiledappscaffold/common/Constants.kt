package com.example.solanamobiledappscaffold.common

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

object Constants {
    const val LAMPORTS_PER_SOL = 1000000000L
    const val dAPP_NAME = "Solana Mobile Dapp Scaffold"
    const val SOLANA_URL = "https://solana.com"
    fun getSolanaExplorerUrl(transactionID: String) = "https://explorer.solana.com/tx/$transactionID?cluster=devnet"

    fun formatBalance(balance: Long): BigDecimal {
        val balanceInLamports = BigDecimal.valueOf(balance)
        val sol = BigDecimal.valueOf(LAMPORTS_PER_SOL)
        val balanceInSol = balanceInLamports.divide(sol, MathContext.DECIMAL128)
        return if (balance % LAMPORTS_PER_SOL == 0L) {
            balanceInSol.setScale(0, RoundingMode.DOWN)
        } else {
            balanceInSol.setScale(4, RoundingMode.DOWN)
        }
    }

    fun formatAddress(publicKey: String): String {
        val firstChars = publicKey.substring(0, 4)
        val lastChars = publicKey.substring(publicKey.length - 4)
        return "$firstChars...$lastChars"
    }
}
