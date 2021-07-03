package ru.avito.util.matcher

import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import java.util.HashSet

public class UniqueMatcher<T> : TypeSafeMatcher<Iterable<T>>() {

    private var duplicate: T? = null

    override fun matchesSafely(item: Iterable<T>): Boolean {
        val set = HashSet<T>()
        val iterator = item.iterator()
        while (iterator.hasNext()) {
            val element = iterator.next()
            if (!set.add(element)) {
                duplicate = element
                return false
            }
        }
        return true
    }

    override fun describeTo(description: Description) {
        description.appendText("contains only unique elements")
    }

    override fun describeMismatchSafely(item: Iterable<T>, mismatchDescription: Description) {
        super.describeMismatchSafely(item, mismatchDescription)
        mismatchDescription
            .appendText(" contains duplicated item: ")
            .appendValue(duplicate)
    }
}

public fun <T> containsOnlyUniqueElements(): UniqueMatcher<T> = UniqueMatcher<T>()
