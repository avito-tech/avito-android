package com.avito.android.rule.internal

import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import com.avito.android.rule.ToastChecks
import com.avito.android.test.waitFor
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.hamcrest.Matchers.empty
import org.hamcrest.TypeSafeMatcher

internal class ProxyToastChecks(
    private val proxyToast: MockProxyToast,
    private val textAccessor: ToastTextAccessor,
) : ToastChecks {

    /**
     * From a hidden class [android.widget.ToastPresenter]
     */
    @Suppress("KDocUnresolvedReference")
    private val longToastDuration = 7000L

    override fun toastDisplayedWithText(text: String) {
        internalToastDisplayedWithText(text)
    }

    override fun toastDisplayedWithText(@StringRes resId: Int) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val text = context.getString(resId)

        internalToastDisplayedWithText(text)
    }

    private fun internalToastDisplayedWithText(text: String) {
        waitFor(
            allowedExceptions = setOf(AssertionError::class.java),
            timeoutMs = longToastDuration
        ) {
            val texts = proxyToast.shownToasts.mapNotNull(textAccessor::text)

            MatcherAssert.assertThat(
                "Toast has shown with text that matches",
                texts,
                Matchers.hasItem(text)
            )
        }
    }

    @Suppress("OverridingDeprecatedMember")
    override fun toastDisplayedWithText(matcher: Matcher<View>) =
        toastDisplayedWithView(matcher)

    override fun toastDisplayedWithView(matcher: Matcher<View>) {
        waitFor(
            allowedExceptions = setOf(AssertionError::class.java),
            timeoutMs = longToastDuration
        ) {
            MatcherAssert.assertThat(
                "Toast has shown with text that matches",
                proxyToast.shownToasts,
                Matchers.hasItem(ToastMatcher(matcher))
            )
        }
    }

    override fun toastNotDisplayed() {
        MatcherAssert.assertThat(
            "There are no toasts shown",
            proxyToast.shownToasts,
            empty()
        )
    }

    private class ToastMatcher(private val viewMatcher: Matcher<View>) : TypeSafeMatcher<Toast>() {

        override fun describeTo(description: Description) {
            description.appendText("Toast not found ")
            description.appendDescriptionOf(viewMatcher)
        }

        override fun matchesSafely(item: Toast): Boolean {
            @Suppress("DEPRECATION")
            return ViewMatchers.hasDescendant(viewMatcher).matches(item.view)
        }
    }

    companion object {

        fun create(proxy: MockProxyToast): ProxyToastChecks {
            return ProxyToastChecks(proxy, ToastTextAccessor.create())
        }
    }
}
