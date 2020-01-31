package com.avito.android.test.matcher

import android.view.View
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

class IsRefreshingMatcher(val matcher: Matcher<Boolean>) :
    BoundedMatcher<View, SwipeRefreshLayout>(SwipeRefreshLayout::class.java) {

    override fun describeTo(description: Description) {
        description.appendText(" SwipeRefreshLayout refreshing state ").appendDescriptionOf(matcher)
    }

    override fun matchesSafely(layout: SwipeRefreshLayout): Boolean {
        return matcher.matches(layout.isRefreshing)
    }
}
