package com.avito.android.snackbar.proxy

import androidx.annotation.VisibleForTesting
import com.google.android.material.snackbar.Snackbar

// VisibleForTesting doesn't work in Kotlin https://issuetracker.google.com/issues/140642032
// Fixed in AS 4.0
@VisibleForTesting
public object SnackbarProxyHolder {
    public var proxy: SnackbarProxy? = null
}

@VisibleForTesting
public interface SnackbarProxy {
    public fun shown(snackbar: Snackbar)
}

public fun Snackbar.showSnackbar() {
    show().apply { SnackbarProxyHolder.proxy?.shown(this@showSnackbar) }
}
