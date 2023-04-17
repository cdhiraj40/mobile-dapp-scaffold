package com.example.solanamobiledappscaffold.domain.use_case.solana_rpc.transactions_usecase

import android.util.Log
import com.example.solanamobiledappscaffold.common.Constants.LAMPORTS_PER_SOL
import com.example.solanamobiledappscaffold.common.Resource
import com.example.solanamobiledappscaffold.data.remote.requests.requestAirdrop
import com.solana.Solana
import com.solana.core.PublicKey
import com.solana.networking.Commitment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException

class RequestAirdropUseCase {
    suspend operator fun invoke(solana: Solana, publicKey: PublicKey): Flow<Resource<String>> =
        flow {
            try {
                emit(Resource.Loading())
                solana.api.requestAirdrop(
                    publicKey,
                    LAMPORTS_PER_SOL,
                    Commitment.CONFIRMED,
                ).onSuccess {
                    emit(Resource.Success(it))
                }.onFailure {
                    Log.e(TAG, it.message.toString())
                    emit(
                        Resource.Error(
                            it.localizedMessage ?: "Something went wrong! Try again later.",
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
        private val TAG = RequestAirdropUseCase::class.java.simpleName
    }
}
