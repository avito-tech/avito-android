package com.avito.instrumentation

import com.avito.instrumentation.internal.InstrumentationTestsActionFactory
import com.avito.instrumentation.internal.verdict.InstrumentationTestsTaskVerdict
import com.github.salomonbrys.kotson.fromJson
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

public class InstrumentationTestsTaskVerdictSerializationTest {

    private val gson = InstrumentationTestsActionFactory.gson

    @Test
    public fun `serialize verdict`() {
        val expected = InstrumentationTestsTaskVerdict(
            title = "Stub title",
            reportUrl = "https://stub-url",
            causeFailureTests = setOf(
                InstrumentationTestsTaskVerdict.Test(
                    testUrl = "https://stub-url",
                    title = "stub test title"
                )
            )
        )

        val actual = gson.fromJson<InstrumentationTestsTaskVerdict>(gson.toJson(expected))

        assertThat(expected)
            .isEqualTo(actual)
    }
}
