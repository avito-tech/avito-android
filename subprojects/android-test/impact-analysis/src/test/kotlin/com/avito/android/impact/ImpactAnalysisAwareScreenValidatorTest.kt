package com.avito.android.impact

import com.google.common.truth.Subject
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class ImpactAnalysisAwareScreenValidatorTest {

    @Test
    fun `validateScreen - error - if id not found in app symbol list`() {
        val result = ImpactAnalysisAwareScreenValidator(
            moduleToIds = mapOf(":lib" to listOf("lescreen_root")),
            idsSymbolList = mapOf("some_other_id" to 26346111)
        ).validateScreen(
            screenClassName = "LeScreen",
            rootId = 21356778,
            module = ":lib"
        )

        assertThat(result).isInstanceOf<ImpactAnalysisAwareScreenValidator.Result.ImpactAnalysisError> {
            assertThat(reason).contains("Can't find id lescreen_root in app's symbol list")
        }
    }

    @Test
    fun `validateScreen - error - if no such module found in project`() {
        val result = ImpactAnalysisAwareScreenValidator(
            moduleToIds = mapOf(":one" to listOf("21356778")),
            idsSymbolList = mapOf()
        ).validateScreen(
            screenClassName = "LeScreen",
            rootId = 21356778,
            module = ":two"
        )

        assertThat(result).isInstanceOf<ImpactAnalysisAwareScreenValidator.Result.ConfigurationError> {
            assertThat(reason).contains("no such module available in project")
        }
    }

    @Test
    fun `validateScreen - error - if no rootID found in module`() {
        val result = ImpactAnalysisAwareScreenValidator(
            moduleToIds = mapOf(
                ":one" to listOf("le_screen_root"),
                ":two" to listOf("some_other_id")
            ),
            idsSymbolList = mapOf(
                "le_screen_root" to 21356778,
                "some_other_id" to 28654122
            )
        ).validateScreen(
            screenClassName = "LeScreen",
            rootId = 21356778,
            module = ":two"
        )

        assertThat(result).isInstanceOf<ImpactAnalysisAwareScreenValidator.Result.ConfigurationError> {
            assertThat(reason).contains("21356778 not found in module")
        }
    }

    @Test
    fun `validateScreen - ok`() {
        val result = ImpactAnalysisAwareScreenValidator(
            moduleToIds = mapOf(
                ":one" to listOf("le_screen_root"),
                ":two" to listOf("some_other_id")
            ),
            idsSymbolList = mapOf(
                "le_screen_root" to 21356778,
                "some_other_id" to 28654122
            )
        ).validateScreen(
            screenClassName = "LeScreen",
            rootId = 28654122,
            module = ":two"
        )

        assertThat(result).isInstanceOf<ImpactAnalysisAwareScreenValidator.Result.OK>()
    }
}

private inline fun <reified T> Subject.isInstanceOf(block: T.() -> Unit = {}) {
    isInstanceOf(T::class.java)
    block(
        this.javaClass
            .getDeclaredField("actual")
            .apply { isAccessible = true }
            .get(this) as T
    )
}
