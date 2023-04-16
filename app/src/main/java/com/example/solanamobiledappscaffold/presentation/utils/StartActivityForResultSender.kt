package com.example.solanamobiledappscaffold.presentation.utils

import android.content.Intent

interface StartActivityForResultSender {
    fun startActivityForResult(
        intent: Intent,
        onActivityCompleteCallback: () -> Unit,
    ) // throws ActivityNotFoundException
}