package com.example.solanamobiledappscaffold.data.remote.requests

import com.solana.api.Api
import com.solana.core.PublicKey
import com.solana.networking.Commitment
import com.solana.networking.RpcRequest
import com.solana.networking.makeRequestResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Represents a JSON-RPC request to get the balance of a Solana account, with a specified commitment level.
 *
 * @param accountAddress The public key of the account to get the balance of.
 * @param commitment The desired commitment level for the balance query.
 */
class GetAirdropRequest(accountAddress: PublicKey, lamports: Long, commitment: Commitment) :
    RpcRequest() {
    override val method: String = "requestAirdrop"
    override val params = buildJsonArray {
        add(accountAddress.toBase58())
        add(lamports)
        add(
            buildJsonObject {
                put("commitment", commitment.value)
            },
        )
    }
}

internal fun requestAirdropSerializer() = String.serializer()

suspend fun Api.requestAirdrop(
    address: PublicKey,
    lamports: Long,
    commitment: Commitment,
): Result<String> =
    router.makeRequestResult(
        GetAirdropRequest(address, lamports, commitment),
        requestAirdropSerializer(),
    )
        .let { result ->
            @Suppress("UNCHECKED_CAST")
            if (result.isSuccess && result.getOrNull() == null) {
                Result.failure(Error("Can not be null"))
            } else {
                result as Result<String>
            }
        }

fun Api.requestAirdrop(
    address: PublicKey,
    lamports: Long,
    commitment: Commitment = Commitment.FINALIZED,
    onComplete: ((Result<String>) -> Unit),
) {
    CoroutineScope(dispatcher).launch {
        onComplete(requestAirdrop(address, lamports, commitment))
    }
}