package com.example.solanamobiledappscaffold.data.remote.dto

import com.example.solanamobiledappscaffold.domain.model.Transaction

data class TransactionDto(
    val transactions: Array<ByteArray>,
) {

    fun toTransaction(): Transaction {
        return Transaction(
            signedTransaction = transactions[0],
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TransactionDto

        if (!transactions.contentDeepEquals(other.transactions)) return false

        return true
    }

    override fun hashCode(): Int {
        return transactions.contentDeepHashCode()
    }
}
