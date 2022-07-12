package com.avito.deeplink_generator

import com.avito.deeplink_generator.internal.validator.PublicDeeplinksValidator
import com.avito.deeplink_generator.model.Deeplink
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class PublicDeeplinksValidatorTest {

    @Test
    fun `equal lists validation - no errors`() {

        val deeplinksFromCode = setOf(Deeplink("ru.avito", "1", "/feed"))
        val deeplinksFromBuildScript = setOf(Deeplink("ru.avito", "1", "/feed"))

        assertDoesNotThrow {
            PublicDeeplinksValidator.validate(deeplinksFromCode, deeplinksFromBuildScript)
        }
    }

    @Test
    fun `different lists validation - not existing in build script error thrown`() {

        val deeplinksFromCode = setOf(Deeplink("ru.avito", "1", "/feed"))
        val deeplinksFromBuildScript = setOf(Deeplink("ru.avito", "2", "/feed"))

        val validationError =
            assertThrows<IllegalStateException> {
                PublicDeeplinksValidator.validate(deeplinksFromCode, deeplinksFromBuildScript)
            }

        assertThat(validationError).hasMessageThat().isEqualTo(
            """
                    Deeplinks are marked as public in code, but not in build script.
                    Such deeplinks: [ru.avito://1/feed] 
                    Please, add code snippet below to this module's build.gradle file:
                    deeplinkGenerator { 
                        publicDeeplinks(
                            "ru.avito://1/feed"
                        )
                    }
        """.trimIndent()
        )
    }

    @Test
    fun `intersecting lists validation - code contains many links from build script - error thrown`() {

        val deeplinksFromCode = setOf(
            Deeplink("ru.avito", "1", "/feed"),
            Deeplink("ru.avito", "1", "/profile"),
            Deeplink("ru.avito", "2", "/feed"),
        )
        val deeplinksFromBuildScript = setOf(Deeplink("ru.avito", "1", "/feed"))

        val validationError =
            assertThrows<IllegalStateException> {
                PublicDeeplinksValidator.validate(deeplinksFromCode, deeplinksFromBuildScript)
            }

        assertThat(validationError).hasMessageThat().isEqualTo(
            """
                    Deeplinks are marked as public in code, but not in build script.
                    Such deeplinks: [ru.avito://1/profile, ru.avito://2/feed] 
                    Please, add code snippet below to this module's build.gradle file:
                    deeplinkGenerator { 
                        publicDeeplinks(
                            "ru.avito://1/profile",
"ru.avito://2/feed"
                        )
                    }
        """.trimIndent()
        )
    }

    @Test
    fun `intersecting lists validation - build script contains links from code - failure`() {

        val deeplinksFromCode = setOf(Deeplink("ru.avito", "1", "/feed"))
        val deeplinksFromBuildScript = setOf(
            Deeplink("ru.avito", "1", "/feed"),
            Deeplink("ru.avito", "2", "/feed")
        )

        val validationError =
            assertThrows<IllegalStateException> {
                PublicDeeplinksValidator.validate(deeplinksFromCode, deeplinksFromBuildScript)
            }

        assertThat(validationError).hasMessageThat().isEqualTo(
            """
                    Deeplinks are marked as public in buildScript, but not in a code.
                    Such deeplinks: [ru.avito://2/feed]

        """.trimIndent()
        )
    }

    @Test
    fun `intersecting lists validation - build script contains links from code - error contains codeFixHint`() {

        val deeplinksFromCode = setOf(Deeplink("ru.avito", "1", "/feed"))
        val deeplinksFromBuildScript = setOf(
            Deeplink("ru.avito", "1", "/feed"),
            Deeplink("ru.avito", "2", "/feed")
        )

        val codeFixHint = "Fix something in your code"

        val validationError =
            assertThrows<IllegalStateException> {
                PublicDeeplinksValidator.validate(deeplinksFromCode, deeplinksFromBuildScript, codeFixHint)
            }

        assertThat(validationError).hasMessageThat().contains(codeFixHint)
    }
}
