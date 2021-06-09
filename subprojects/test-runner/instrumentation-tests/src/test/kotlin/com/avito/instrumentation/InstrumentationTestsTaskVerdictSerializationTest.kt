package com.avito.instrumentation

import com.avito.runner.finalizer.verdict.InstrumentationTestsTaskVerdict
import com.google.common.truth.Truth.assertThat
import com.google.gson.GsonBuilder
import org.junit.jupiter.api.Test

public class InstrumentationTestsTaskVerdictSerializationTest {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    @Test
    public fun `serialize verdict`() {
        val expected = InstrumentationTestsTaskVerdict(
            title = "Stub title",
            reportUrl = "https://stub-url",
            problemTests = setOf(
                InstrumentationTestsTaskVerdict.Test(
                    testUrl = "https://stub-url",
                    title = "stub test title"
                )
            )
        )

        val actual = gson.fromJson(
            gson.toJson(expected),
            InstrumentationTestsTaskVerdict::class.java
        )

        assertThat(expected)
            .isEqualTo(actual)
    }
}
