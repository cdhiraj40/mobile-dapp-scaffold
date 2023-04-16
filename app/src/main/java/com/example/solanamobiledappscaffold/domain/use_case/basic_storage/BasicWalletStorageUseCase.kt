package com.example.solanamobiledappscaffold.domain.use_case.basic_storage

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.net.toUri
import com.example.solanamobiledappscaffold.domain.model.Wallet
import java.math.BigDecimal
import javax.inject.Inject

class BasicWalletStorageUseCase @Inject constructor(context: Context) {
    private val preference: SharedPreferences

    val publicKey58: String?
        get() = preference.getString(PUBLIC_KEY_VALUE58, null)
    
    val publicKey64: String?
        get() = preference.getString(PUBLIC_KEY_VALUE64, null)

    val balance: BigDecimal
        get() = preference.getString(BALANCE_VALUE, null)?.toBigDecimal() ?: BigDecimal(0)

    val walletURI: Uri?
        get() = preference.getString(WALLET_URI_VALUE, null)?.toUri()

    val authToken: String?
        get() = preference.getString(AUTH_TOKEN, null)

    init {
        preference = context.getSharedPreferences(WALLET_PREFS, Context.MODE_PRIVATE)
    }

    fun saveWallet(wallet: Wallet) {
        val editor = preference.edit()
        editor.putString(PUBLIC_KEY_VALUE58, wallet.publicKey58)
        editor.putString(PUBLIC_KEY_VALUE64, wallet.publicKey64)
        editor.putString(BALANCE_VALUE, wallet.balance.toString())
        editor.putString(WALLET_URI_VALUE, wallet.walletUriBase.toString())
        editor.putString(AUTH_TOKEN, wallet.authToken)
        editor.apply()
    }

    fun getWallet(): Wallet {
        return Wallet(
            publicKey58 = preference.getString(PUBLIC_KEY_VALUE58, null)
                ?: throw Exception("Public key is null"),
            publicKey64 = preference.getString(PUBLIC_KEY_VALUE64, null)
                ?: throw Exception("Public key is null"),
            balance = preference.getString(BALANCE_VALUE, null)?.toBigDecimal() ?: BigDecimal(0),
            walletUriBase = preference.getString(WALLET_URI_VALUE, null)?.toUri(),
            authToken = preference.getString(AUTH_TOKEN, null),
        )
    }

    fun clearWallet() {
        val editor = preference.edit()
        editor.remove(PUBLIC_KEY_VALUE58)
        editor.remove(BALANCE_VALUE)
        editor.remove(WALLET_URI_VALUE)
        editor.remove(AUTH_TOKEN)
        editor.apply()
    }

    fun updatePublicKey58(publicKey: String) {
        val editor = preference.edit()
        editor.putString(PUBLIC_KEY_VALUE58, publicKey)
        editor.apply()
    }
    
    fun updatePublicKey64(publicKey: String) {
        val editor = preference.edit()
        editor.putString(PUBLIC_KEY_VALUE64, publicKey)
        editor.apply()
    }

    fun updateBalance(balance: String) {
        val editor = preference.edit()
        editor.putString(BALANCE_VALUE, balance.toString())
        editor.apply()
    }

    fun updateWalletURI(walletURI: Uri) {
        val editor = preference.edit()
        editor.putString(WALLET_URI_VALUE, walletURI.toString())
        editor.apply()
    }

    fun updateAuthToken(authToken: String) {
        val editor = preference.edit()
        editor.putString(AUTH_TOKEN, authToken)
        editor.apply()
    }

    companion object {
        private const val WALLET_PREFS = "WalletPrefs"
        private const val PUBLIC_KEY_VALUE58 = "public_key_58_value"
        private const val PUBLIC_KEY_VALUE64 = "public_key_64_value"
        private const val BALANCE_VALUE = "balance_value"
        private const val WALLET_URI_VALUE = "wallet_uri_value"
        private const val AUTH_TOKEN = "auth_token_value"
    }
}