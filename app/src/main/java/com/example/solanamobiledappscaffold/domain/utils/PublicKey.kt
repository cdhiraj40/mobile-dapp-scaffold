package com.example.solanamobiledappscaffold.domain.utils

import android.util.Base64
import org.bitcoinj.core.Base58

// need this for correct public key
fun toBase58(pubkey: ByteArray): String {
    return Base58.encode(pubkey)
}

// need this for signing messages
fun toBase64(pubkey: ByteArray): String {
    return Base64.encode(pubkey, Base64.NO_WRAP).decodeToString()
}

fun String.toBase64(): ByteArray {
    return Base64.decode(this, Base64.DEFAULT)
}