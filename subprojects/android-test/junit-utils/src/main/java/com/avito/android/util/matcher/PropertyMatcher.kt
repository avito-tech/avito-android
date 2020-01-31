package com.avito.android.util.matcher

import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.StringDescription
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties


typealias Predicate<T> = (T) -> Boolean
typealias Assertion<T> = (Predicate<T>) -> Unit


class PropertyMatcher<T : Any>(
    private val obj: T,
    private val otherObj: T,
    private val linePrefix: String = "",
    private val matchAll: Boolean = true
) {
    private val matches: MutableMap<String, MatchResult> by lazy {
        if (matchAll) {
            obj::class.memberProperties
                .associate { prop ->
                    val thisValue = prop.getter.call(obj)
                    val thatValue = if (otherObj::class.memberProperties.contains(prop)) {
                        prop.getter.call(otherObj)
                    } else {
                        println("Class ${otherObj::class.java.canonicalName} doesn't have property '${prop.name}'")
                        null
                    }
                    val mismatchDescription = StringDescription()
                    val matcher = Matchers.equalTo(thisValue)
                    matcher.describeMismatch(thatValue, mismatchDescription)
                    prop.name to MatchResult(
                        matches = matcher.matches(thatValue),
                        mismatchDescription = mismatchDescription.toString()
                    )
                }
                .toMutableMap()
        } else {
            obj::class.memberProperties
                .associate { prop ->
                    prop.name to MatchResult(
                        matches = true,
                        mismatchDescription = "Property is ignored"
                    )
                }
                .toMutableMap()
        }
    }


    fun <T> isInstanceOf(clazz: Class<T>) {
        matches[".class"] = MatchResult(
            matches = clazz.isAssignableFrom(obj.javaClass),
            mismatchDescription = "Expected ${clazz.canonicalName} got ${obj.javaClass.canonicalName}"
        )
    }

    fun <P : Any?> KProperty1<T, P>.ignore() {
        if (matches.containsKey(name)) {
            matches[name] = MatchResult(
                matches = true,
                mismatchDescription = "Property is ignored"
            )
        } else {
            throw IllegalArgumentException("Class ${obj::class.qualifiedName} doesn't have a property with name '$name'!")
        }
    }

    infix fun <P : Any?> KProperty1<T, P>.valueEquals(expectedVal: P) {
        if (matches.containsKey(name)) {
            val actualValue = get(obj)
            matches[name] = MatchResult(
                matches = actualValue == expectedVal,
                mismatchDescription = "Values are not equal: expected = $expectedVal, actual = $actualValue"
            )
        } else {
            throw IllegalArgumentException("Class ${obj::class.qualifiedName} doesn't have a property with name '$name'!")
        }
    }

    infix fun <P : Any?> KProperty1<T, P>.valueMatches(predicate: Predicate<P>) {
        if (matches.containsKey(name)) {
            val actualValue = get(obj)

            matches[name] = MatchResult(
                matches = predicate(actualValue),
                mismatchDescription = "Value doesn't match predicate. actualValue = $actualValue"
            )
        } else {
            throw IllegalArgumentException("Class ${obj::class.qualifiedName} doesn't have a property with name '$name'!")
        }
    }

    fun <P : Any?> KProperty0<P>.ignore() {
        if (matches.containsKey(name)) {
            matches[name] = MatchResult(
                matches = true,
                mismatchDescription = "Property is ignored"
            )
        } else {
            throw IllegalArgumentException("Class ${obj::class.qualifiedName} doesn't have a property with name '$name'!")
        }
    }

    infix fun <P : Any?> KProperty0<P>.valueEquals(expectedVal: P) {
        if (matches.containsKey(name)) {
            val actualValue = get()

            matches[name] = MatchResult(
                matches = actualValue == expectedVal,
                mismatchDescription = "Values are not equal: expected = $expectedVal, actual = $actualValue"
            )
        } else {
            throw IllegalArgumentException("Class ${obj::class.qualifiedName} doesn't have a property with name '$name'!")
        }
    }

    infix fun <P : Any?> KProperty0<P>.valueMatches(predicate: Predicate<P>) {
        if (matches.containsKey(name)) {
            val actualValue = get()

            matches[name] = MatchResult(
                matches = predicate(actualValue),
                mismatchDescription = "Value doesn't match predicate. actualValue = $actualValue"
            )
        } else {
            throw IllegalArgumentException("Class ${obj::class.qualifiedName} doesn't have a property with name '$name'!")
        }
    }

    infix fun <P : Any?> KProperty0<P>.valueMatches(matcher: Matcher<P>) {
        if (matches.containsKey(name)) {
            val mismatchDescription = StringDescription()
            val actualValue = get()
            matcher.describeMismatch(actualValue, mismatchDescription)

            matches[name] = MatchResult(
                matches = matcher.matches(actualValue),
                mismatchDescription = mismatchDescription.toString()
            )
        } else {
            throw IllegalArgumentException("Class ${obj::class.qualifiedName} doesn't have a property with name '$name'!")
        }
    }

    infix fun <P : Any> KProperty0<P>.propertyMatches(modifications: PropertyMatcher<P>.(P) -> Unit) {
        if (matches.containsKey(name)) {
            val actualValue = get()
            val otherValue = otherObj::class.memberProperties.first { it.name == name }.getter.call(otherObj)

            @Suppress("UNCHECKED_CAST")
            matches[name] = PropertyMatcher(actualValue, otherValue as P, "\t$linePrefix")
                .apply {
                    modifications(actualValue)
                }
                .matchResult()
        } else {
            throw IllegalArgumentException("Class ${obj::class.qualifiedName} doesn't have a property with name '$name'!")
        }
    }

    infix fun <P : Any> KProperty0<P>.propertyMatchesOnly(modifications: PropertyMatcher<P>.(P) -> Unit) {
        if (matches.containsKey(name)) {
            val actualValue = get()
            val otherValue = otherObj::class.memberProperties.first { it.name == name }.getter.call(otherObj)

            @Suppress("UNCHECKED_CAST")
            matches[name] = PropertyMatcher(actualValue, otherValue as P, "\t$linePrefix", matchAll = false)
                .apply {
                    modifications(actualValue)
                }
                .matchResult()
        } else {
            throw IllegalArgumentException("Class ${obj::class.qualifiedName} doesn't have a property with name '$name'!")
        }
    }

    fun <P : Iterable<I>, I : Any> KProperty0<P>.itemPropertyMatches(
        index: Int,
        matchAll: Boolean = true,
        modifications: PropertyMatcher<I>.(I) -> Unit
    ) {
        if (matches.containsKey(name)) {
            val actualValue = get().toList()[index]
            val otherList = otherObj::class.memberProperties.first { it.name == name }.getter.call(otherObj)

            @Suppress("UNCHECKED_CAST")
            matches[name] = PropertyMatcher(actualValue, (otherList as P).toList()[index], "\t$linePrefix", matchAll)
                .apply {
                    modifications(actualValue)
                }
                .matchResult(prefixLine = "List items at index $index do not match:\n")
        } else {
            throw IllegalArgumentException("Class ${obj::class.qualifiedName} doesn't have a property with name '$name'!")
        }
    }

    fun match(): Boolean {
        val result = matchResult()
        if (!result.matches) {
            println(result.mismatchDescription)
        }
        return result.matches
    }

    private fun matchResult(prefixLine: String = ""): MatchResult {
        return if (matches.values.map { it.matches }.contains(false)) {
            val result = StringBuilder()
            result.append("$linePrefix$prefixLine")
            result.append("${linePrefix}Following properties don't match:\n")
            matches.filterValues { !it.matches }
                .forEach { (name, matchResult) ->
                    result.append("$linePrefix\t$name: ${matchResult.mismatchDescription}\n")
                }
            MatchResult(false, result.toString())
        } else {
            MatchResult(true, "")
        }
    }
}

private data class MatchResult(val matches: Boolean, val mismatchDescription: String)

fun <T : Any> Assertion<T>.matches(
    otherObj: T,
    modifications: PropertyMatcher<T>.(T) -> Unit = {}
) = invoke { obj: T ->
    PropertyMatcher(obj, otherObj).apply { modifications(obj) }.match()
}

fun <T : Any> Assertion<T>.matchesOnly(
    otherObj: T,
    modifications: PropertyMatcher<T>.(T) -> Unit = {}
) = invoke { obj: T ->
    PropertyMatcher(obj, otherObj, matchAll = false).apply { modifications(obj) }.match()
}
