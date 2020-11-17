package com.avito.android.test.matcher

import android.view.View
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

/**
 * Used to refactor PageObjectElement matcher constructor
 */
class NoViewMatcher : TypeSafeMatcher<View>() {
    override fun describeTo(description: Description) {
        description.appendText(
            " If you see this description: you use legacy PageObjectElement constructor; " +
                "It is forbidden for now, because it doesn't respect interaction context"
        )
    }

    override fun matchesSafely(item: View): Boolean = false
}
