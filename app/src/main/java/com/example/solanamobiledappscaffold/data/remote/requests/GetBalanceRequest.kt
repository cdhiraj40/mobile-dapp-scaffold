package com.example.solanamobiledappscaffold.data.remote.requests

import com.solana.api.Api
import com.solana.core.PublicKey
import com.solana.networking.Commitment
import com.solana.networking.RpcRequest
import com.solana.networking.makeRequestResult
import com.solana.networking.serialization.serializers.solana.SolanaResponseSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.*

/**
 * Represents a JSON-RPC request to get the balance of a Solana account, with a specified commitment level.
 *
 * @param accountAddress The public key of the account to get the balance of.
 * @param commitment The desired commitment level for the balance query.
 */
class GetBalanceRequest(accountAddress: String, commitment: Commitment) : RpcRequest() {
    override val method: String = "getBalance"
    override val params = buildJsonArray {
        add(accountAddress)
        add(
            buildJsonObject {
                put("commitment", commitment.value)
            },
        )
    }
}

internal fun getBalanceSerializer() = SolanaResponseSerializer(Long.serializer())

suspend fun Api.getBalance(account: PublicKey, commitment: Commitment): Result<Long> =
    router.makeRequestResult(
        GetBalanceRequest(account.toBase58(), commitment),
        getBalanceSerializer(),
    )
        .let { result ->
            @Suppress("UNCHECKED_CAST")
            if (result.isSuccess && result.getOrNull() == null) {
                Result.failure(Error("Can not be null"))
            } else {
                result as Result<Long> // safe cast, null case handled above
            }
        }

fun Api.getBalance(
    account: PublicKey,
    commitment: Commitment = Commitment.FINALIZED,
    onComplete: ((Result<Long>) -> Unit),
) {
    CoroutineScope(dispatcher).launch {
        onComplete(getBalance(account, commitment))
    }
}
