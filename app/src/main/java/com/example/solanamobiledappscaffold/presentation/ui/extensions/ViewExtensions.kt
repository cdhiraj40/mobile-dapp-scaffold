package com.example.solanamobiledappscaffold.presentation.ui.extensions

import android.view.View
import com.google.android.material.snackbar.Snackbar

fun View.showSnackbar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
    // Create a new Snackbar object with the given message and duration
    Snackbar.make(this, message, duration).show()
}

fun View.showSnackbarWithAction(
    message: String,
    actionMessage: String = "Copy",
    duration: Int = Snackbar.LENGTH_LONG,
    action: (() -> Unit),
) {
    Snackbar.make(this, message, duration).setAction(
        actionMessage,
    ) {
        action()
    }.show()
}
