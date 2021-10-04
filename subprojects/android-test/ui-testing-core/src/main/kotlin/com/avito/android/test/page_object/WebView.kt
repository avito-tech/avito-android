package com.avito.android.test.page_object

import android.view.View
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.model.Atom
import androidx.test.espresso.web.model.Atoms.getCurrentUrl
import androidx.test.espresso.web.model.ElementReference
import androidx.test.espresso.web.sugar.Web
import com.avito.android.test.action.WebElementActions
import com.avito.android.test.checks.WebElementChecks
import org.hamcrest.Matcher
import org.hamcrest.Matchers.containsString
import java.util.concurrent.TimeUnit

open class WebView(private val webViewMatcher: Matcher<View>) {

    private val interaction: Web.WebInteraction<Void>
        get() = Web.onWebView(webViewMatcher)

    val checks = WebViewChecksImpl()

    fun withElement(
        elementMatcher: Atom<ElementReference>,
        timeoutSeconds: Long = WEB_ELEMENT_TIMEOUT_SECONDS
    ) = WebViewElement(elementMatcher, timeoutSeconds)

    interface WebViewChecks {

        fun withUrl(uri: String)
    }

    inner class WebViewChecksImpl : WebViewChecks {

        override fun withUrl(uri: String) {
            interaction.check(webMatches(getCurrentUrl(), containsString(uri)))
        }
    }

    inner class WebViewElement(
        private val elementMatcher: Atom<ElementReference>,
        private val timeoutSeconds: Long
    ) {

        val actions: WebElementActions
            get() = WebElementActions(
                interaction.withTimeout(timeoutSeconds, TimeUnit.SECONDS).withElement(
                    elementMatcher
                )
            )

        val checks: WebElementChecks
            get() = WebElementChecks(
                interaction.withTimeout(timeoutSeconds, TimeUnit.SECONDS).withElement(
                    elementMatcher
                )
            )
    }
}

private const val WEB_ELEMENT_TIMEOUT_SECONDS = 5L
