package com.avito.android.test.matcher

import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import androidx.test.espresso.AmbiguousViewMatcherException
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.core.internal.deps.guava.base.Predicate
import androidx.test.espresso.core.internal.deps.guava.collect.Iterables
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.util.TreeIterables.breadthFirstViewTraversal
import org.hamcrest.Matcher
import org.hamcrest.Matchers.equalTo
import org.hamcrest.StringDescription
import kotlin.math.abs

object AvitoPositionAssertions {

    fun isCenteredVerticallyWith(viewMatcher: Matcher<View>) =
        ViewAssertion { view, noViewFoundException ->
            noViewFoundCheck(noViewFoundException)

            val viewLocation = VerticalCenter(view)
            val otherViewLocation = VerticalCenter(viewMatcher, getTopViewGroup(view))
            assertLocationEqualWithTolerance(viewLocation, otherViewLocation)
        }

    fun isCenteredVerticallyWithBottomOf(viewMatcher: Matcher<View>) =
        ViewAssertion { view, noViewFoundException ->
            noViewFoundCheck(noViewFoundException)

            val viewLocation = VerticalCenter(view)
            val otherViewLocation = Bottom(viewMatcher, getTopViewGroup(view))
            assertLocationEqualWithTolerance(viewLocation, otherViewLocation)
        }

    private fun noViewFoundCheck(noViewFoundException: NoMatchingViewException?) {
        val description = StringDescription()
        if (noViewFoundException != null) {
            description.appendText(
                String.format(
                    "' check could not be performed because view '%s' was not found.\n",
                    noViewFoundException.viewMatcherDescription
                )
            )
            throw noViewFoundException
        }
    }

    private fun assertLocationEqualWithTolerance(
        location1: ScreenLocation,
        location2: ScreenLocation
    ) {
        val failDescription = StringDescription()
        failDescription.appendText(location1.description)
            .appendText(String.format(" (%d)", location1.absoluteLocation))
            .appendText(" is not equal to ")
            .appendText(location2.description)
            .appendText(String.format(" (%d) ", location2.absoluteLocation))

        assertThat(
            failDescription.toString(),
            abs(location1.absoluteLocation - location2.absoluteLocation)
                < PIXEL_COMPARISON_TOLERANCE,
            equalTo(true)
        )
    }

    private interface ScreenLocation {
        val description: String
        val absoluteLocation: Int
    }

    private class VerticalCenter(
        private val view: View,
        override val description: String = "vertical center"
    ) : ScreenLocation {

        override val absoluteLocation: Int
            get() {
                val location = IntArray(2)
                view.getLocationOnScreen(location)
                return location[1] + view.height / 2
            }

        constructor(viewMatcher: Matcher<View>, root: View?) : this(
            findView(viewMatcher, root),
            "vertical center of $viewMatcher"
        )
    }

    private class Bottom(
        private val view: View,
        override val description: String = "bottom"
    ) : ScreenLocation {

        override val absoluteLocation: Int
            get() {
                val location = IntArray(2)
                view.getLocationOnScreen(location)
                return location[1] + view.height
            }

        constructor(viewMatcher: Matcher<View>, root: View?) : this(
            findView(viewMatcher, root),
            "bottom of $viewMatcher"
        )
    }

    private fun getTopViewGroup(view: View): ViewGroup? {
        var currentParent: ViewParent? = view.parent
        var topView: ViewGroup? = null
        while (currentParent != null) {
            if (currentParent is ViewGroup) {
                topView = currentParent
            }
            currentParent = currentParent.parent
        }
        return topView
    }

    private fun findView(toView: Matcher<View>, root: View?): View {
        val viewPredicate = Predicate<View> { input -> toView.matches(input) }
        val matchedViewIterator =
            Iterables.filter(breadthFirstViewTraversal(root), viewPredicate).iterator()
        var matchedView: View? = null
        while (matchedViewIterator.hasNext()) {
            if (matchedView != null) {
                throw AmbiguousViewMatcherException.Builder()
                    .withRootView(root)
                    .withViewMatcher(toView)
                    .withView1(matchedView)
                    .withView2(matchedViewIterator.next())
                    .withOtherAmbiguousViews(
                        *matchedViewIterator.asSequence()
                            .toList()
                            .toTypedArray()
                    )
                    .build()
            } else {
                matchedView = matchedViewIterator.next()
            }
        }
        if (matchedView == null) {
            throw NoMatchingViewException.Builder()
                .withViewMatcher(toView)
                .withRootView(root)
                .build()
        }
        return matchedView
    }
}

private const val PIXEL_COMPARISON_TOLERANCE = 2
