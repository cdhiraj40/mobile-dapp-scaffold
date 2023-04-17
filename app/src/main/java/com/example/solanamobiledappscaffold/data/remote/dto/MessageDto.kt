package com.example.solanamobiledappscaffold.data.remote.dto

import com.example.solanamobiledappscaffold.domain.model.Message

data class MessageDto(
    val signedMessages: Array<ByteArray>,
) {
    override fun toString(): String {
        return "SignPayloadsResult{signedPayload=${signedMessages.contentToString()}}"
    }

    fun toMessage(): Message {
        return Message(
            signedMessage = signedMessages[0],
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MessageDto

        if (!signedMessages.contentDeepEquals(other.signedMessages)) return false

        return true
    }

    override fun hashCode(): Int {
        return signedMessages.contentDeepHashCode()
    }
}