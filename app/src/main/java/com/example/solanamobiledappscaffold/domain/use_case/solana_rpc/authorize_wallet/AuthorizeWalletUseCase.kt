package com.example.solanamobiledappscaffold.domain.use_case.solana_rpc.authorize_wallet

import android.util.Log
import com.example.solanamobiledappscaffold.common.Resource
import com.example.solanamobiledappscaffold.domain.model.Wallet
import com.example.solanamobiledappscaffold.domain.repository.WalletRepository
import com.solana.mobilewalletadapter.clientlib.protocol.JsonRpc20Client
import com.solana.mobilewalletadapter.clientlib.protocol.MobileWalletAdapterClient
import com.solana.mobilewalletadapter.common.ProtocolContract
import java.io.IOException
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeoutException
import javax.inject.Inject

class AuthorizeWalletUseCase @Inject constructor(private val walletRepository: WalletRepository) {
    suspend operator fun invoke(client: MobileWalletAdapterClient): Resource<Wallet> {
        try {
            val wallet = walletRepository.authorize(client).toWallet()
            return Resource.Success(wallet)
        } catch (e: ExecutionException) {
            when (val cause = e.cause) {
                is IOException -> {
                    Log.e(TAG, "IO error while sending authorize", cause)
                    return Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
                }
                is TimeoutException -> {
                    Log.e(TAG, "Timed out while waiting for authorize result", cause)
                    return Resource.Error(e.localizedMessage ?: "Timed out request")
                }
                is JsonRpc20Client.JsonRpc20RemoteException -> return when (cause.code) {
                    ProtocolContract.ERROR_AUTHORIZATION_FAILED -> {
                        Log.e(TAG, "Not authorized", cause)
                        Resource.Error(e.localizedMessage ?: "Not authorized")
                    }
                    ProtocolContract.ERROR_CLUSTER_NOT_SUPPORTED -> {
                        Log.e(TAG, "Cluster not supported", cause)
                        Resource.Error(e.localizedMessage ?: "Cluster not supported")
                    }
                    else -> {
                        Log.e(TAG, "Remote exception for authorize", cause)
                        Resource.Error(e.localizedMessage ?: "Something went wrong")
                    }
                }
                is MobileWalletAdapterClient.InsecureWalletEndpointUriException -> {
                    Log.e(TAG, "authorize result contained a non-HTTPS wallet base URI", e)
                    return Resource.Error(e.localizedMessage ?: "Something went wrong")
                }
                is JsonRpc20Client.JsonRpc20Exception -> {
                    Log.e(
                        TAG,
                        "JSON-RPC client exception for authorize",
                        cause,
                    )
                    return Resource.Error(e.localizedMessage ?: "Something went wrong")
                }
                else -> {
                    return Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
                }
            }
        } catch (e: CancellationException) {
            Log.e(TAG, "authorize request was cancelled", e)
            return Resource.Error(e.localizedMessage ?: "Request was cancelled. Please try again!")
        } catch (e: InterruptedException) {
            Log.e(TAG, "authorize request was interrupted", e)
            return Resource.Error(
                e.localizedMessage ?: "Request was interrupted. Please try again!",
            )
        }
    }

    companion object {
        private val TAG = AuthorizeWalletUseCase::class.java.simpleName
    }
}
