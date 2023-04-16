package com.example.solanamobiledappscaffold.domain.model

import android.util.Base64

data class SignPayloadResult(
    val signedPayload: ByteArray,
) {

    override fun toString(): String {
        return Base64.encodeToString(signedPayload, Base64.NO_WRAP)
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SignPayloadResult

        if (!signedPayload.contentEquals(other.signedPayload)) return false

        return true
    }

    override fun hashCode(): Int {
        return signedPayload.contentHashCode()
    }
}
