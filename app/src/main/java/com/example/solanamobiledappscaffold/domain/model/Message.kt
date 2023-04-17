package com.example.solanamobiledappscaffold.domain.model

import android.util.Base64

data class Message(
    val signedMessage: ByteArray,
) {

    override fun toString(): String {
        return Base64.encodeToString(signedMessage, Base64.NO_WRAP)
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Message

        if (!signedMessage.contentEquals(other.signedMessage)) return false

        return true
    }

    override fun hashCode(): Int {
        return signedMessage.contentHashCode()
    }
}
