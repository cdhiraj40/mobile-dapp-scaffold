package com.example.solanamobiledappscaffold.data.remote.requests

import com.solana.api.Api
import com.solana.networking.Commitment
import com.solana.networking.RpcRequest
import com.solana.networking.makeRequestResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class LatestBlockhashRequest : RpcRequest() {
    override val method: String = "getLatestBlockhash"
    override val params = buildJsonArray {
        add(
            buildJsonObject {
                put("commitment", Commitment.FINALIZED.value)
            },
        )
    }
}

@Serializable
internal data class BlockhashResponse(val blockhash: String, val lastValidBlockHeight: Long)

internal fun blockhashSerializer() =
    SolanaResponseSerializer(BlockhashResponse.serializer())

suspend fun Api.getLatestBlockhash(): Result<String> =
    router.makeRequestResult(LatestBlockhashRequest(), blockhashSerializer()).let { result ->
        @Suppress("UNCHECKED_CAST")
        if (result.isSuccess && result.getOrNull() == null) {
            Result.failure(Error("Can not be null"))
        } else {
            result.map { it?.blockhash!! } // safe cast, null case handled above
        }
    }

fun Api.getLatestBlockhash(onComplete: ((Result<String>) -> Unit)) {
    CoroutineScope(dispatcher).launch {
        onComplete(getLatestBlockhash())
    }
}

class SolanaResponseSerializer<R>(dataSerializer: KSerializer<R>) :
    KSerializer<R?> {
    private val serializer = WrappedValue.serializer(dataSerializer)
    override val descriptor: SerialDescriptor = serializer.descriptor

    override fun serialize(encoder: Encoder, value: R?) =
        encoder.encodeSerializableValue(serializer, WrappedValue(value))

    override fun deserialize(decoder: Decoder): R? =
        decoder.decodeSerializableValue(serializer).value
}

@Serializable
private class WrappedValue<V>(val value: V?)