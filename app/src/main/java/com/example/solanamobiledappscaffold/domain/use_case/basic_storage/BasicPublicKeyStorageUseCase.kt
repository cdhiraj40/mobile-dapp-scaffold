package com.example.solanamobiledappscaffold.domain.use_case.basic_storage

import android.content.Context
import android.content.SharedPreferences

class BasicPublicKeyStorageUseCase(context: Context) {
    private val preference: SharedPreferences

    init {
        preference = context.getSharedPreferences(PUBLIC_KEY_PREFS, Context.MODE_PRIVATE)
    }

    fun savePublicKey(publicKey: String) {
        val editor = preference.edit()
        editor.putString(PUBLIC_KEY_VALUE, publicKey)
        editor.apply()
    }

    val publicKey: String?
        get() = preference.getString(PUBLIC_KEY_VALUE, null)

    companion object {
        private const val PUBLIC_KEY_PREFS = "PublicKeyPrefs"
        private const val PUBLIC_KEY_VALUE = "public_key_value"
    }
}