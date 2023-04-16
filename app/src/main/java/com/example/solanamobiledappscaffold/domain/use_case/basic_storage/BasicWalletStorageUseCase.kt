package com.example.solanamobiledappscaffold.domain.use_case.basic_storage

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.net.toUri
import java.math.BigDecimal
import javax.inject.Inject

class BasicWalletStorageUseCase @Inject constructor(context: Context) {
    private val preference: SharedPreferences

    val publicKey: String?
        get() = preference.getString(PUBLIC_KEY_VALUE, null)
    
    val balance: BigDecimal
        get() = preference.getString(BALANCE_VALUE, null)?.toBigDecimal() ?: BigDecimal(0)
    
    val walletURI: Uri?
        get() = preference.getString(WALLET_URI_VALUE, null)?.toUri()
    
    val authToken: String?
        get() = preference.getString(AUTH_TOKEN, null)
    
    init {
        preference = context.getSharedPreferences(WALLET_PREFS, Context.MODE_PRIVATE)
    }

    fun savePublicKey(publicKey: String) {
        val editor = preference.edit()
        editor.putString(PUBLIC_KEY_VALUE, publicKey)
        editor.apply()
    }

    fun clearPublicKey() {
        val editor = preference.edit()
        editor.remove(PUBLIC_KEY_VALUE)
        editor.apply()
    }
    
    fun saveBalance(balance: String) {
        val editor = preference.edit()
        editor.putString(BALANCE_VALUE, balance)
        editor.apply()
    }

    fun clearBalance() {
        val editor = preference.edit()
        editor.remove(BALANCE_VALUE)
        editor.apply()
    }
    
    fun saveWalletURI(walletURI: String) {
        val editor = preference.edit()
        editor.putString(WALLET_URI_VALUE, walletURI)
        editor.apply()
    }

    fun clearWalletURI() {
        val editor = preference.edit()
        editor.remove(WALLET_URI_VALUE)
        editor.apply()
    }
    
    fun saveAuthToken(authToken: String) {
        val editor = preference.edit()
        editor.putString(AUTH_TOKEN, authToken)
        editor.apply()
    }

    fun clearAuthToken() {
        val editor = preference.edit()
        editor.remove(AUTH_TOKEN)
        editor.apply()
    }

    companion object {
        private const val WALLET_PREFS = "WalletPrefs"
        private const val PUBLIC_KEY_VALUE = "public_key_value"
        private const val BALANCE_VALUE = "balance_value"
        private const val WALLET_URI_VALUE = "wallet_uri_value"
        private const val AUTH_TOKEN = "auth_token_value"
    }
}