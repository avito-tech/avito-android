package com.avito.android.test.app.second

import android.annotation.SuppressLint
import android.widget.TextView
import com.avito.android.rule.SimpleRule
import com.avito.android.snackbar.proxy.SnackbarProxy
import com.avito.android.snackbar.proxy.SnackbarProxyHolder
import com.avito.android.test.waitFor
import com.google.android.material.snackbar.Snackbar
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import ru.avito.util.Is
import com.google.android.material.R as google_R

interface SnackbarAsserts {
    fun assertIsShownWith(text: String)
    fun assertIsShownWith(text: Matcher<String>)
    fun assertIsShownLastWith(text: String)
    fun assertIsShownLastWith(text: Matcher<String>)
}

class SnackbarRule() : SimpleRule(), SnackbarAsserts {

    private val proxy = Proxy()

    override fun before() {
        SnackbarProxyHolder.proxy = proxy
    }

    fun clear() {
        proxy.snackbarTexts.clear()
    }

    override fun assertIsShownWith(text: String) {
        assertIsShownWith(Is(text))
    }

    override fun assertIsShownLastWith(text: String) {
        assertIsShownLastWith(Is(text))
    }

    override fun assertIsShownWith(text: Matcher<String>) {
        waitFor {
            MatcherAssert.assertThat(
                "Snackbar with text matches $text wasn't shown",
                proxy.snackbarTexts,
                Matchers.hasItem(text)
            )
        }
    }

    override fun assertIsShownLastWith(text: Matcher<String>) {
        waitFor {
            val last = proxy.snackbarTexts.lastOrNull() ?: throw AssertionError("There weren't shown any snackbar")
            MatcherAssert.assertThat(
                "Snackbar with text mathes $text wasn't shown last",
                last,
                text
            )
        }
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
