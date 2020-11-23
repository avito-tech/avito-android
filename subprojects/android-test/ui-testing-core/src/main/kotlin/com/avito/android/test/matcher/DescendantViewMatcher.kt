package com.avito.android.test.matcher

import android.view.View
import androidx.test.espresso.AmbiguousViewMatcherException
import androidx.test.espresso.util.TreeIterables
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

/**
 * Copied from [ViewFinderHelper]
 */
internal class DescendantViewMatcher(private val matcher: Matcher<View>) : TypeSafeMatcher<View>() {

    override fun describeTo(description: Description) {
        description.appendText("Match has descendant view: ")
            .appendDescriptionOf(matcher)
    }

    override fun matchesSafely(item: View): Boolean {
        val matchedViewsSequence: Sequence<View> = TreeIterables.breadthFirstViewTraversal(item)
            .asSequence()
            .filter { matcher.matches(it) }

        val matchedViews = matchedViewsSequence.toList()

        return when {
            matchedViews.isEmpty() -> false
            matchedViews.size == 1 -> true
            else -> {

                throw AmbiguousViewMatcherException.Builder()
                    .withViewMatcher(matcher)
                    .withRootView(item)
                    .withView1(matchedViews[0])
                    .withView2(matchedViews[1])
                    .apply {
                        if (matchedViews.size > 2) {
                            withOtherAmbiguousViews(
                                *matchedViews.takeLast(matchedViews.size - 2).toTypedArray()
                            )
                        }
                    }
                    .build()
            }
        }
    }
}
