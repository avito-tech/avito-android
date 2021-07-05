package com.avito.android.test.matcher

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

class RecyclerViewMatcher {

    fun itemsInList(countMatcher: Matcher<Int>): Matcher<View> {
        return object : RecyclerViewMatcher() {

            private var actualCount: Int? = null

            override fun performMatching(recyclerView: RecyclerView): Boolean {
                actualCount = recyclerView.adapter?.itemCount ?: 0

                return countMatcher.matches(actualCount)
            }

            override fun describeTo(description: Description) {
                super.describeTo(description)
                if (actualCount != null) {
                    description.appendText("actual count in list is $actualCount")
                }
            }
        }
    }

    fun firstVisibleItemPosition(positionMatcher: Matcher<Int>): Matcher<View> {
        return object : RecyclerViewMatcher() {

            private var actualPosition: Int? = null

            override fun performMatching(recyclerView: RecyclerView): Boolean {
                actualPosition = recyclerView.getChildAdapterPosition(recyclerView.getChildAt(0))

                return positionMatcher.matches(actualPosition)
            }

            override fun describeTo(description: Description) {
                super.describeTo(description)
                if (actualPosition != null) {
                    description.appendText("first visible item position is $actualPosition")
                }
            }
        }
    }

    fun hasViewTypeAtPosition(position: Int, viewType: Int): Matcher<View> = object : RecyclerViewMatcher() {
        override fun performMatching(recyclerView: RecyclerView): Boolean {
            val adapter = recyclerView.adapter ?: error("Adapter is not attached to RecyclerView")
            return adapter.getItemViewType(position) == viewType
        }
    }

    fun doesNotHaveViewTypeAtPosition(position: Int, viewType: Int) = not(hasViewTypeAtPosition(position, viewType))

    private abstract class RecyclerViewMatcher : TypeSafeMatcher<View>() {

        private var actualView: View? = null

        final override fun matchesSafely(view: View?): Boolean {
            if (view !is RecyclerView) {
                actualView = view
                return false
            }
            return performMatching(view)
        }

        protected abstract fun performMatching(recyclerView: RecyclerView): Boolean

        override fun describeTo(description: Description) {
            if (actualView != null) {
                description.appendText("You are trying to match RecyclerView on $actualView")
            }
        }
    }
}
