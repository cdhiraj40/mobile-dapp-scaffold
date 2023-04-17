package com.example.solanamobiledappscaffold.domain.use_case.solana_rpc.transactions_usecase

import android.util.Log
import com.example.solanamobiledappscaffold.common.Resource
import com.example.solanamobiledappscaffold.data.remote.requests.getBalance
import com.solana.Solana
import com.solana.core.PublicKey
import com.solana.networking.Commitment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException

class BalanceUseCase {
    suspend operator fun invoke(
        solana: Solana,
        publicKey: PublicKey,
        commitment: Commitment,
    ): Flow<Resource<Long>> =
        flow {
            try {
                emit(Resource.Loading())
                solana.api.getBalance(
                    publicKey,
                    commitment,
                ).onSuccess {
                    emit(Resource.Success(it))
                }.onFailure {
                    Log.e(TAG, it.message.toString())
                    emit(
                        Resource.Error(
                            it.localizedMessage
                                ?: "An unexpected error occurred. Please try again later!",
                        ),
                    )
                }
            } catch (e: IOException) {
                Log.e(TAG, e.message.toString())
                emit(Resource.Error("Couldn't reach server. Check your internet connection!"))
            } catch (e: Exception) {
                Log.e(TAG, e.message.toString())
                emit(
                    Resource.Error(
                        e.localizedMessage
                            ?: "An unexpected error occurred. Please try again later!",
                    ),
                )
            }
        }

    companion object {
        private val TAG = BalanceUseCase::class.java.simpleName
    }
}
