package com.example.solanamobiledappscaffold.domain.repository

import com.example.solanamobiledappscaffold.data.remote.dto.MessageDto
import com.example.solanamobiledappscaffold.data.remote.dto.TransactionDto
import com.example.solanamobiledappscaffold.data.remote.dto.WalletDto
import com.solana.mobilewalletadapter.clientlib.protocol.MobileWalletAdapterClient

interface WalletRepository {

    suspend fun authorize(client: MobileWalletAdapterClient): WalletDto

    suspend fun signMessage(
        client: MobileWalletAdapterClient,
        messages: Array<ByteArray>,
        addresses: Array<ByteArray>,
    ): MessageDto

    suspend fun sendTransaction(
        client: MobileWalletAdapterClient,
        transactions: Array<ByteArray>,
    ): TransactionDto
}
