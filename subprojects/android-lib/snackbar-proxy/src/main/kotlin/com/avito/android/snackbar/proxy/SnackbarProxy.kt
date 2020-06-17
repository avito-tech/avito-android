package com.avito.android.snackbar.proxy

import androidx.annotation.VisibleForTesting
import com.google.android.material.snackbar.Snackbar

// VisibleForTesting doesn't work in Kotlin https://issuetracker.google.com/issues/140642032
// Fixed in AS 4.0
@VisibleForTesting
object SnackbarProxyHolder {
    var proxy: SnackbarProxy? = null
}

@VisibleForTesting
interface SnackbarProxy {
    fun shown(snackbar: Snackbar)
}

fun Snackbar.showSnackbar() {
    show().apply { SnackbarProxyHolder.proxy?.shown(this@showSnackbar) }
}
