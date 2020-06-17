package com.avito.android.test.app.second

import com.avito.android.test.waitFor
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert
import org.hamcrest.TypeSafeMatcher
import ru.avito.util.Is

class SnackbarChecks internal constructor(
    private val proxy: SnackbarRule.Proxy
) {

    fun isShownWithTextMatches(text: Matcher<String>) {
        isShownWithTextMatches(text, 1)
    }

    fun isShownWithExactlyText(text: String) {
        isShownWithTextMatches(Is(text))
    }

    fun isShownLastWithTextMatches(text: Matcher<String>) {
        waitFor {
            val last = proxy.snackbarTexts.lastOrNull() ?: throw AssertionError("There weren't shown any snackbar")
            MatcherAssert.assertThat(
                "Snackbar with text mathes $text wasn't shown last",
                last,
                text
            )
        }
    }

    fun isShownLastWithExactlyText(text: String) {
        isShownLastWithTextMatches(Is(text))
    }

    fun isShownWithTextMatches(text: Matcher<String>, times: Int) {
        waitFor {
            MatcherAssert.assertThat(
                "Snackbar with text matches $text wasn't shown $times times",
                proxy.snackbarTexts,
                HasItemTimes(text, times)
            )
        }
    }

    private class HasItemTimes(
        private val item: Matcher<String>,
        private val times: Int
    ) : TypeSafeMatcher<Collection<String>>() {

        override fun describeTo(description: Description) {
            description
                .appendText("a collection contains ")
                .appendDescriptionOf(item)
                .appendText(" $times times")
        }

        override fun matchesSafely(items: Collection<String>): Boolean {
            return items.count { item.matches(it) } == times
        }
    }
}
