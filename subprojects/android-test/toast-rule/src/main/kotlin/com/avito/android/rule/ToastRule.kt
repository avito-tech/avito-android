package com.avito.android.rule

import android.view.View
import android.widget.Toast
import androidx.test.espresso.matcher.ViewMatchers
import com.avito.android.test.waitFor
import com.avito.android.util.PlatformProxyToast
import com.avito.android.util.ProxyToast
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.hasItem
import org.hamcrest.TypeSafeMatcher
import java.util.concurrent.TimeUnit

class ToastRule : SimpleRule() {

    private val mockProxyToast: MockProxyToast = MockProxyToast(original = PlatformProxyToast())

    override fun before() {
        mockProxyToast.clear()
        ProxyToast.instance = mockProxyToast
    }

    fun toastDisplayedAtLeastOnceWithText(matcher: Matcher<String>) = wrapToastCheck {
        mockProxyToast.toastDisplayedAtLeastOnceWithText(ViewMatchers.withText(matcher))
    }

    fun toastDisplayedWithText(resId: Int) = wrapToastCheck {
        mockProxyToast.toastDisplayedWithText(ViewMatchers.withText(resId))
    }

    fun toastDisplayedWithText(text: String) = wrapToastCheck {
        mockProxyToast.toastDisplayedWithText(ViewMatchers.withText(text))
    }

    fun toastNotDisplayed() = wrapToastCheck {
        mockProxyToast.toastNotDisplayed()
    }

    fun clearRecordedInvocations() {
        mockProxyToast.clear()
    }

    private fun wrapToastCheck(realCheck: () -> Unit): Unit {
        waitFor(
            allowedExceptions = setOf(AssertionError::class.java),
            timeoutMs = TimeUnit.SECONDS.toMillis(4)
        ) {
            realCheck.invoke()
        }
    }

    private class MockProxyToast(private val original: ProxyToast) : ProxyToast {

        private val shownToasts: MutableList<Toast> = mutableListOf()

        override fun show(toast: Toast) {
            shownToasts += toast
            original.show(toast)
        }

        fun toastDisplayedWithText(matcher: Matcher<View>) {
            assertThat(
                "Only one toast has shown text that matches",
                shownToasts,
                Matchers.allOf(
                    Matchers.iterableWithSize(1),
                    hasItem(withView(matcher))
                )
            )
        }

        fun toastDisplayedAtLeastOnceWithText(matcher: Matcher<View>) {
            assertThat(
                "Toast has shown with text that matches",
                shownToasts,
                hasItem(withView(matcher))
            )
        }

        fun toastNotDisplayed() {
            assertThat(
                "There are no toasts shown",
                shownToasts,
                empty()
            )
        }

        fun clear() {
            shownToasts.clear()
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
}
