package com.example.solanamobiledappscaffold.data.remote.dto

import android.net.Uri
import com.example.solanamobiledappscaffold.domain.model.Wallet
import org.bitcoinj.core.Base58

data class WalletDto(
    val authToken: String?,
    val publicKey: ByteArray?,
    val accountLabel: String?,
    val walletUriBase: Uri?,
) {

    fun toWallet(): Wallet {
        return Wallet(
            publicKey = requireNotNull(publicKey) { "Public key is null" }.let(::toBase58),
        )
    }

    private fun toBase58(pubkey: ByteArray): String {
        return Base58.encode(pubkey)
    }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WalletDto

        if (authToken != other.authToken) return false
        if (publicKey != null) {
            if (other.publicKey == null) return false
            if (!publicKey.contentEquals(other.publicKey)) return false
        } else if (other.publicKey != null) return false
        if (accountLabel != other.accountLabel) return false
        if (walletUriBase != other.walletUriBase) return false

        return true
    }

    override fun hashCode(): Int {
        var result = authToken?.hashCode() ?: 0
        result = 31 * result + (publicKey?.contentHashCode() ?: 0)
        result = 31 * result + (accountLabel?.hashCode() ?: 0)
        result = 31 * result + (walletUriBase?.hashCode() ?: 0)
        return result
    }
}
