package com.example.solanamobiledappscaffold.domain.use_case.solana_rpc.sign_transaction

import android.util.Log
import com.example.solanamobiledappscaffold.common.Resource
import com.example.solanamobiledappscaffold.domain.model.Transaction
import com.example.solanamobiledappscaffold.domain.repository.WalletRepository
import com.solana.mobilewalletadapter.clientlib.protocol.JsonRpc20Client
import com.solana.mobilewalletadapter.clientlib.protocol.MobileWalletAdapterClient
import com.solana.mobilewalletadapter.common.ProtocolContract
import java.io.IOException
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeoutException
import javax.inject.Inject

class SendTransactionUseCase @Inject constructor(private val walletRepository: WalletRepository) {
    suspend operator fun invoke(
        client: MobileWalletAdapterClient,
        transactions: Array<ByteArray>,
    ): Resource<Transaction> {
        try {
            val transaction = walletRepository.sendTransaction(client, transactions).toTransaction()
            return Resource.Success(transaction)
        } catch (e: ExecutionException) {
            when (val cause = e.cause) {
                is IOException -> {
                    Log.e(TAG, "IO error while sending authorize", cause)
                    return Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
                }
                is TimeoutException -> {
                    Log.e(
                        TAG,
                        "Timed out while waiting for authorize result",
                        cause,
                    )
                    return Resource.Error(e.localizedMessage ?: "Timed out request")
                }
                is MobileWalletAdapterClient.InvalidPayloadsException -> {
                    Log.e(TAG, "Message payloads invalid", cause)
                    return Resource.Error(e.localizedMessage ?: "Message payloads invalid")
                }
                is JsonRpc20Client.JsonRpc20RemoteException -> return when (cause.code) {
                    ProtocolContract.ERROR_AUTHORIZATION_FAILED -> {
                        Log.e(
                            TAG,
                            "Authorization invalid, authorization or reauthorization required",
                            cause,
                        )
                        Resource.Error(e.localizedMessage ?: "Not authorized")
                    }
                    ProtocolContract.ERROR_NOT_SIGNED -> {
                        Log.e(TAG, "User did not authorize signing", cause)
                        Resource.Error(e.localizedMessage ?: "User did not authorize signing")
                    }
                    ProtocolContract.ERROR_TOO_MANY_PAYLOADS -> {
                        Log.e(TAG, "Too many payloads to sign", cause)
                        Resource.Error(e.localizedMessage ?: "Too many payloads to sign")
                    }
                    else -> {
                        Log.e(TAG, "Remote exception for sign messages", cause)
                        Resource.Error(e.localizedMessage ?: "Something went wrong")
                    }
                }
                is JsonRpc20Client.JsonRpc20Exception -> {
                    Log.e(
                        TAG,
                        "JSON-RPC client exception for sign messages",
                        cause,
                    )
                    return Resource.Error(e.localizedMessage ?: "Something went wrong")
                }
                else -> {
                    return Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
                }
            }
        } catch (e: CancellationException) {
            Log.e(TAG, "sign message request was cancelled", e)
            return Resource.Error(e.localizedMessage ?: "Request was cancelled. Please try again!")
        } catch (e: InterruptedException) {
            Log.e(TAG, "sign message request was interrupted", e)
            return Resource.Error(
                e.localizedMessage ?: "Request was interrupted. Please try again!",
            )
        }
    }

    companion object {
        private val TAG = SendTransactionUseCase::class.java.simpleName
    }
}
