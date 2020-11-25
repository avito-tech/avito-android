package com.avito.android.util.matcher

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import java.lang.Character.isWhitespace

class IsEqualIgnoringAllWhiteSpace(private val expectedString: String) : TypeSafeMatcher<String>() {

    override fun matchesSafely(item: String): Boolean {
        return expectedString.stripSpace() == item.stripSpace()
    }

    override fun describeMismatchSafely(item: String, mismatchDescription: Description) {
        mismatchDescription.appendText("was ").appendText("\"$item\"")
    }

    override fun describeTo(description: Description) {
        description.appendText("a string equal to \"$expectedString\" ignoring all white spaces")
    }

    fun String.stripSpace() = this.filter { !isWhitespace(it) }

    companion object {
        fun equalToIgnoringAllWhiteSpace(expectedString: String): Matcher<String> {
            return IsEqualIgnoringAllWhiteSpace(expectedString)
        }
    }
}
