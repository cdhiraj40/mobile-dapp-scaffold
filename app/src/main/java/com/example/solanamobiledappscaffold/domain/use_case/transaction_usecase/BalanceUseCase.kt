package com.example.solanamobiledappscaffold.domain.use_case.transaction_usecase

import com.example.solanamobiledappscaffold.common.Resource
import com.example.solanamobiledappscaffold.data.remote.getBalance
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
                    commitment
                ).onSuccess {
                    emit(Resource.Success(it))
                }.onFailure {
                    emit(
                        Resource.Error(
                            it.message.toString()
                                ?: "An unexpected error occurred. Please try again later!"
                        )
                    )
                }
            } catch (e: IOException) {
                emit(Resource.Error("Couldn't reach server. Check your internet connection!"))
            } catch (e: Exception) {
                emit(
                    Resource.Error(
                        e.message.toString()
                            ?: "An unexpected error occurred. Please try again later!"
                    )
                )
            }
        }
}
