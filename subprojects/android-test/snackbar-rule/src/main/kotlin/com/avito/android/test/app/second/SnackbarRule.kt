package com.avito.android.test.app.second

import android.annotation.SuppressLint
import android.widget.TextView
import com.avito.android.rule.SimpleRule
import com.avito.android.snackbar.proxy.SnackbarProxy
import com.avito.android.snackbar.proxy.SnackbarProxyHolder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.R as google_R

class SnackbarRule : SimpleRule() {

    private val proxy = Proxy()

    val checks = SnackbarChecks(proxy)

    override fun before() {
        SnackbarProxyHolder.proxy = proxy
    }

    fun clear() {
        proxy.snackbarTexts.clear()
    }

    @SuppressLint("VisibleForTests")
    internal class Proxy : SnackbarProxy {
        val snackbarTexts = mutableListOf<String>()

        override fun shown(snackbar: Snackbar) {
            snackbarTexts.add(
                snackbar.text
            )
        }

        // look at snackbar.setText
        private val Snackbar.text: String
            get() = view.findViewById<TextView>(google_R.id.snackbar_text).text.toString()

    }
}
