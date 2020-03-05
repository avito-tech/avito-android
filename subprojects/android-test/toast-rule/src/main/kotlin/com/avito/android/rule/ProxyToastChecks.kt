package com.avito.android.rule

import android.view.View
import android.widget.Toast
import androidx.test.espresso.matcher.ViewMatchers
import com.avito.android.test.waitFor
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher

internal class ProxyToastChecks(private val proxyToast: MockProxyToast) : ToastChecks {

    private val longToastDuration = 3500L

    override fun toastDisplayedWithText(text: String) {
        toastDisplayedWithText(ViewMatchers.withText(text))
    }

    override fun toastDisplayedWithText(resId: Int) {
        toastDisplayedWithText(ViewMatchers.withText(resId))
    }

    override fun toastDisplayedWithText(matcher: Matcher<View>) {
        waitFor(
            allowedExceptions = setOf(AssertionError::class.java),
            timeoutMs = longToastDuration
        ) {
            MatcherAssert.assertThat(
                "Toast has shown with text that matches",
                proxyToast.shownToasts,
                Matchers.hasItem(withView(matcher))
            )
        }
    }

    override fun toastNotDisplayed() {
        MatcherAssert.assertThat(
            "There are no toasts shown",
            proxyToast.shownToasts,
            Matchers.empty()
        )
    }

    private fun withView(viewMatcher: Matcher<View>): Matcher<Toast> = object : TypeSafeMatcher<Toast>() {
        override fun describeTo(description: Description) {
            description.appendText("Toast not found ")
            description.appendDescriptionOf(viewMatcher)
        }

        override fun matchesSafely(item: Toast): Boolean {
            return ViewMatchers.hasDescendant(viewMatcher).matches(item.view)
        }
    }
}
