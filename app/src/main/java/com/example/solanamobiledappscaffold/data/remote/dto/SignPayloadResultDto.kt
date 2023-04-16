package com.example.solanamobiledappscaffold.data.remote.dto

import com.example.solanamobiledappscaffold.domain.model.SignPayloadResult

data class SignPayloadResultDto(
    val signedPayload: Array<ByteArray>,
) {
    override fun toString(): String {
        return "SignPayloadsResult{signedPayload=${signedPayload.contentToString()}}"
    }

    fun toSignPayloadResult(): SignPayloadResult {
        return SignPayloadResult(
            signedPayload = signedPayload[0],
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SignPayloadResultDto

        if (!signedPayload.contentDeepEquals(other.signedPayload)) return false

        return true
    }

    override fun hashCode(): Int {
        return signedPayload.contentDeepHashCode()
    }
}