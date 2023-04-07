package com.example.solanamobiledappscaffold.data.repository

import android.net.Uri
import android.util.Log
import com.example.solanamobiledappscaffold.common.Constants
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
            Constants.SOLANA,
            ProtocolContract.CLUSTER_DEVNET,
        ).get()

        Log.d(WalletRepository::class.java.simpleName, "Authorized: $result")

        return WalletDto(
            authToken = result.authToken,
            publicKey = result.publicKey,
            accountLabel = result.accountLabel,
            walletUriBase = result.walletUriBase,
        )
    }
}
