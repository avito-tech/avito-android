package com.avito.android.util.matcher

import org.hamcrest.Matcher

/**
 * @see default matchers for Map in [org.hamcrest.Matchers]
 */
object MapMatchers {

    fun <K, V> isEmpty() = hasSize<K, V>(0)

    fun <K, V> hasSize(size: Int): Matcher<Map<K, V>> {
        return MapSizeMatcher(size)
    }

    fun <K, V> containsAll(map: Map<K, V>): Matcher<Map<K, V>> {
        return MapContainsMapMatcher(map)
    }

}

