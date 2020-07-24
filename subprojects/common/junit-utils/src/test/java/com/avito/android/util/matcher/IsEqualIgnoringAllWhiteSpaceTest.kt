package com.avito.android.util.matcher

import com.avito.android.util.matcher.IsEqualIgnoringAllWhiteSpace.Companion.equalToIgnoringAllWhiteSpace
import org.hamcrest.Matcher
import org.junit.jupiter.api.Test

class IsEqualIgnoringAllWhiteSpaceTest : AbstractMatcherTest() {

    private val THIN_SPACE = '\u2009'
    private val matcher = equalToIgnoringAllWhiteSpace(" Hello World$THIN_SPACE  how\n are we? ")

    override fun createMatcher(): Matcher<*> {
        return matcher
    }

    @Test
    fun `matches - on same words with different whitespaces`() {
        assertMatches(matcher, "Hello World how are we?")
        assertMatches(matcher, "   Hello World   how are \n\n\twe?")
    }

    @Test
    fun `matches - only whitespaces order differs, ignoring words`() {
        assertMatches(matcher, "HelloWorld${THIN_SPACE}how are we?")
        assertMatches(matcher, "Hello Wo rld how are we?")
        assertMatches(matcher, "HelloWorldhowarewe?")
        assertMatches(matcher, "   HelloWorldhowarewe?    ")
        assertMatches(matcher, "   Hel\nloWorld\nhowar\new\te?   \n  ")
    }

    @Test
    fun `does not match - differs other than whitespaces`() {
        assertDoesNotMatch(matcher, "Hello PLANET how are we?")
        assertDoesNotMatch(matcher, "Hello World how are we")
    }

    @Test
    fun `does not match - case differs`() {
        assertDoesNotMatch(matcher, "Hello WORLD how are we?")
    }

    @Test
    fun `does not match - on null passed`() {
        assertDoesNotMatch(matcher, null)
    }

    @Test
    fun `has a readable mismatch`() {
        assertMismatchDescription("was \"Hello World how are we \"", matcher, "Hello World how are we ")
    }

    @Test
    fun `has a readable description`() {
        assertDescription(
            "a string equal to \" Hello World$THIN_SPACE  how\n" +
                    " are we? \" ignoring all white spaces", matcher
        )
    }
}
