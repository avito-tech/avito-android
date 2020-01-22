package com.avito.android.util.matcher

import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class MapContainsMapMatcher<K, V>(private val map: Map<K, V>) : TypeSafeMatcher<Map<K, V>>() {

    override fun describeMismatchSafely(
        item: Map<K, V>,
        mismatchDescription: Description
    ) {
        mismatchDescription
            .appendText("not all entries are found from ")
            .appendValue(item)
    }

    override fun describeTo(description: Description) {
        description.appendText("Map ")
            .appendValue(map)
            .appendText(" should contains all entries from another map")
    }

    override fun matchesSafely(target: Map<K, V>): Boolean {
        map.forEach { entry ->
            if (target[entry.key] != entry.value) return false
        }
        return true
    }

}