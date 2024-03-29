package com.avito.android.ui

import android.view.View

internal fun View.toggleVisibility() {
    visibility = if (visibility == View.GONE) {
        View.VISIBLE
    } else {
        View.GONE
    }
}
