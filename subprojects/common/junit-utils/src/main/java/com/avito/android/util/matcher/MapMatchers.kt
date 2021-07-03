package com.avito.android.util.matcher

import org.hamcrest.Matcher

/**
 * @see default matchers for Map in [org.hamcrest.Matchers]
 */
public object MapMatchers {

    public fun <K, V> isEmpty(): Matcher<Map<K, V>> = hasSize<K, V>(0)

    public fun <K, V> hasSize(size: Int): Matcher<Map<K, V>> {
        return MapSizeMatcher(size)
    }

    public fun <K, V> containsAll(map: Map<K, V>): Matcher<Map<K, V>> {
        return MapContainsMapMatcher(map)
    }
}
