package com.example.solanamobiledappscaffold.data.repository

import android.net.Uri
import android.util.Log
import com.example.solanamobiledappscaffold.common.Constants
import com.example.solanamobiledappscaffold.data.remote.dto.MessageDto
import com.example.solanamobiledappscaffold.data.remote.dto.TransactionDto
import com.example.solanamobiledappscaffold.data.remote.dto.WalletDto
import com.example.solanamobiledappscaffold.domain.repository.WalletRepository
import com.solana.mobilewalletadapter.clientlib.protocol.MobileWalletAdapterClient
import com.solana.mobilewalletadapter.common.ProtocolContract
import javax.inject.Inject

class WalletRepositoryImpl @Inject constructor() : WalletRepository {
    override suspend fun authorize(client: MobileWalletAdapterClient): WalletDto {
        val result = client.authorize(
            Uri.parse(Constants.SOLANA_URL),
            Uri.parse("favicon.ico"),
            Constants.dAPP_NAME,
            ProtocolContract.CLUSTER_DEVNET,
        ).get()

        Log.d(TAG, "Authorized: $result")

        return WalletDto(
            authToken = result.authToken,
            publicKey = result.publicKey,
            accountLabel = result.accountLabel,
            walletUriBase = result.walletUriBase,
        )
    }

    override suspend fun signMessage(
        client: MobileWalletAdapterClient,
        messages: Array<ByteArray>,
        addresses: Array<ByteArray>,
    ): MessageDto {
        val result = client.signMessages(
            messages,
            addresses,
        ).get()

        Log.d(TAG, "Authorized: $result")
        
        return MessageDto(
            signedMessages = result.signedPayloads,
        )
    }

    override suspend fun sendTransaction(
        client: MobileWalletAdapterClient, 
        transactions: Array<ByteArray>,
    ): TransactionDto {
        val result = client.signTransactions(
            transactions,
        ).get()

        Log.d(TAG, "Authorized: $result")
        
        return TransactionDto(
            transactions = result.signedPayloads,
        )
    }

    companion object {
        private val TAG = WalletRepository::class.java.simpleName
    }
}
