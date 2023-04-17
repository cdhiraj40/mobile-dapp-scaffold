package com.example.solanamobiledappscaffold.domain.model

import org.bitcoinj.core.Base58

data class Transaction(
    val signedTransaction: ByteArray,
) {

    override fun toString(): String {
        return Base58.encode(signedTransaction)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Transaction

        if (!signedTransaction.contentEquals(other.signedTransaction)) return false

        return true
    }

    override fun hashCode(): Int {
        return signedTransaction.contentHashCode()
    }
}