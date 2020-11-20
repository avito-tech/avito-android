package ru.avito.util.matcher

import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import kotlin.reflect.KClass

class ContainsValueWithTypeMatcher<T : Any>(private val klass: KClass<T>) : TypeSafeMatcher<Iterable<Any>>() {

    override fun matchesSafely(item: Iterable<Any>): Boolean {
        return item.any { klass.java.isInstance(it) }
    }

    override fun describeTo(description: Description?) {
        description?.appendText("contains item with type ")
            ?.appendValue(klass)
    }
}

inline fun <reified T : Any> containsValueWithType() = ContainsValueWithTypeMatcher(T::class)
