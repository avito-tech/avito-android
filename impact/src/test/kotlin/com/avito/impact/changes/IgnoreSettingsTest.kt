package com.avito.impact.changes

import com.avito.impact.changes.IgnoreSettingsTest.Case.Matched
import com.avito.impact.changes.IgnoreSettingsTest.Case.NotMatched
import com.google.common.truth.Truth.assertWithMessage
import org.junit.jupiter.api.Test

class IgnoreSettingsTest {

    sealed class Case(val pattern: String, val path: String) {
        class Matched(pattern: String, path: String) : Case(pattern, path)
        class NotMatched(pattern: String, path: String) : Case(pattern, path)
    }

    @Test
    fun patterns() {
        // just simple integration checks for contract, see other cases in the library itself
        listOf(
            NotMatched("*.md", "build.gradle"),
            NotMatched("*.md", "project/build.gradle"),

            Matched("*.md", "README.md"),
            Matched("*.md", "project/README.md"),

            Matched(".idea/**", ".idea/config.xml"),
            Matched(".idea/**", ".idea/codeStyles/Project.xml")
        ).forEach { case ->
            val settings = IgnoreSettings(setOf(case.pattern))

            val expectedResult: String? = if (case is Matched) case.pattern else null

            assertWithMessage("${case.pattern} -> ${case.path}")
                .that(settings.match(case.path)).isEqualTo(expectedResult)
        }
    }
}
