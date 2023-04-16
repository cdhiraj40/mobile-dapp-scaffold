package com.example.solanamobiledappscaffold.presentation.ui.extensions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

fun Context.copyToClipboard(label: String = "", text: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
}