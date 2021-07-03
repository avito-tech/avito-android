package com.avito.android.util.matcher

import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeDiagnosingMatcher

/**
 * Matcher for exception cause of any level
 * Usable if you care about specific type + message of exception cause, but doesn't care about level of this cause
 *
 * to be used in ExpectedException.exceptCause(DeepCauseMatcher(...))
 *
 * @param maxDepth depth=1 is root exception's cause and so on.
 *                 maxDepth needed to stop recursion at some point for failure cases
 */
public class DeepCauseMatcher<T>(
    private val typeMatcher: Matcher<T>,
    private val expectedMessage: Matcher<String>,
    private val maxDepth: Int
) : TypeSafeDiagnosingMatcher<Throwable?>() {

    override fun matchesSafely(item: Throwable?, mismatchDescription: Description): Boolean {
        return check(item, mismatchDescription.appendText("\n"), 1)
    }

    public fun check(item: Throwable?, mismatch: Description, depth: Int): Boolean {
        return when (item) {
            null -> {
                mismatch.appendText("Throwable cause at level: $depth is null")
                false
            }
            else -> {
                val typeMatch = typeMatcher.matches(item)
                val messageMatch = expectedMessage.matches(item.message)
                when {
                    !typeMatch || !messageMatch -> {
                        mismatch.appendText("Throwable cause at level: $depth is not matching: [")
                        typeMatcher.describeMismatch(item.javaClass, mismatch)
                        mismatch.appendText("] and [")
                        expectedMessage.describeMismatch(item.message, mismatch)
                        mismatch.appendText("]\n")
                        when {
                            depth < maxDepth -> check(item.cause, mismatch, depth + 1)
                            else -> {
                                mismatch.appendText("Stopping search: reached max level of depth")
                                false
                            }
                        }
                    }
                    else -> true
                }
            }
        }
    }

    override fun describeTo(description: Description): Unit = with(description) {
        appendText("Throwable's cause that is [")
        appendDescriptionOf(typeMatcher)
        appendText("] and [has ")
        appendDescriptionOf(expectedMessage)
        appendText("], trying to go deeper recursive with a maximum depth of ")
        appendValue(maxDepth)
    }

    public companion object {

        /**
         * matches cause of any deep up to [maxDepth]
         * that is instanceOf [T]
         * and has message containing [partOfExpectedMessage]
         */
        public inline fun <reified T : Throwable> deepCauseMatcher(
            partOfExpectedMessage: String,
            maxDepth: Int = 5
        ): DeepCauseMatcher<T> {
            val type = T::class.java

            return DeepCauseMatcher(
                CoreMatchers.instanceOf(type),
                containsString(partOfExpectedMessage),
                maxDepth
            )
        }
    }
}
