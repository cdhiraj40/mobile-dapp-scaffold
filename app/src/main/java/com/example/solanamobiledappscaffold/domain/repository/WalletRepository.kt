package com.example.solanamobiledappscaffold.domain.repository

import com.example.solanamobiledappscaffold.data.remote.dto.WalletDto
import com.solana.mobilewalletadapter.clientlib.protocol.MobileWalletAdapterClient

interface WalletRepository {

    suspend fun authorize(client: MobileWalletAdapterClient): WalletDto

//    suspend fun createWallet(): CoinDetailDto
//
//    suspend fun getWallet(): List<CoinDto>
}
