package com.avito.android.rule.internal

import android.widget.Toast
import com.avito.android.util.ProxyToast

internal class MockProxyToast(private val original: ProxyToast) : ProxyToast {

    internal val shownToasts: MutableList<Toast> = mutableListOf()

    override fun show(toast: Toast) {
        shownToasts += toast
        original.show(toast)
    }

    fun clear() {
        shownToasts.clear()
    }
}
