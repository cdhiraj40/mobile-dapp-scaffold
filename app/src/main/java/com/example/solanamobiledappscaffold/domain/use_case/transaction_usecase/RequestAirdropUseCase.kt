package com.example.solanamobiledappscaffold.domain.use_case.transaction_usecase

import com.example.solanamobiledappscaffold.common.Constants.LAMPORTS_PER_SOL
import com.example.solanamobiledappscaffold.common.Resource
import com.solana.Solana
import com.solana.api.requestAirdrop
import com.solana.core.PublicKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException

class RequestAirdropUseCase {
    suspend operator fun invoke(solana: Solana, publicKey: PublicKey): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.Loading())
            solana.api.requestAirdrop(
                publicKey,
                LAMPORTS_PER_SOL
            ).onSuccess {
                emit(Resource.Success(Unit))
            }.onFailure {
                emit(
                    Resource.Error(
                        it.localizedMessage ?: "Something went wrong! Try again later."
                    )
                )
            }
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection!"))
        } catch (e: Exception) {
            emit(
                Resource.Error(
                    e.localizedMessage ?: "An unexpected error occurred. Please try again later!"
                )
            )
        }
    }
}
