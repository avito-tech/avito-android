package com.avito.android.test.app.second

import android.annotation.SuppressLint
import android.widget.TextView
import com.avito.android.rule.SimpleRule
import com.avito.android.snackbar.proxy.SnackbarProxy
import com.avito.android.snackbar.proxy.SnackbarProxyHolder
import com.avito.android.test.waitFor
import com.google.android.material.snackbar.Snackbar
import org.junit.Assert
import com.google.android.material.R as google_R

class SnackbarProxyRule : SimpleRule() {

    private val proxy = Proxy()

    override fun before() {
        SnackbarProxyHolder.proxy = proxy
    }

    fun isShown(text: String) {
        waitFor {
            Assert.assertTrue("Snackbar with text=\"$text\" wasn't shown", proxy.snackbarTexts.any { it == text })
        }
    }

    fun isShownLast(text: String) {
        waitFor {
            Assert.assertTrue(
                "Snackbar with text=\"$text\" wasn't shown last",
                text == proxy.snackbarTexts.lastOrNull { it == text })
        }
    }

    fun isShownTimes(text: String, times: Int) {
        waitFor {
            Assert.assertTrue(
                "Snackbar with text=\"$text\" wasn't shown $times times",
                times == proxy.snackbarTexts.count { it == text })
        }
    }

    fun clear() {
        proxy.snackbarTexts.clear()
    }

    @SuppressLint("VisibleForTests")
    private class Proxy : SnackbarProxy {
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
