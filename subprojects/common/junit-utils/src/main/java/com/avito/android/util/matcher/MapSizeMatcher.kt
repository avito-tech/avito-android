package com.avito.android.util.matcher

import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class MapSizeMatcher<K, V>(private val size: Int) : TypeSafeMatcher<Map<K, V>>() {

    override fun describeMismatchSafely(
        item: Map<K, V>,
        mismatchDescription: Description
    ) {
        mismatchDescription
            .appendText("was with size ")
            .appendValue(item.size)
    }

    override fun describeTo(description: Description) {
        description.appendText("map with size of ").appendValue(size)
    }

    override fun matchesSafely(map: Map<K, V>): Boolean = (map.size == size)
}
