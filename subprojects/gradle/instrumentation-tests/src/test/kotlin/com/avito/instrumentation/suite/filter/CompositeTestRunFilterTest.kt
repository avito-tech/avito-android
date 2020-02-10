package com.avito.instrumentation.suite.filter

import com.avito.instrumentation.suite.dex.AnnotationData
import com.avito.instrumentation.suite.dex.TestInApk
import com.avito.instrumentation.suite.dex.createStubInstance
import com.avito.report.model.DeviceName
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.isInstanceOf
import org.junit.jupiter.api.Test

internal class CompositeTestRunFilterTest {

    @Test
    fun `runNeeded - returns first skip occurrence - when multiple possible`() {
        val verdict = checkIfRunNeeded(
            CompositeTestRunFilter(
                listOf(
                    IgnoredAnnotationFilter(setOf("Dont.Run.Me")),
                    AnnotatedWithFilter(listOf("Run.Me"))
                )
            )
        )

        assertThat(verdict).isInstanceOf<TestRunFilter.Verdict.Skip.Ignored>()
    }

    @Test
    fun `check if another test works`() {
        val verdict = checkIfRunNeeded(
            CompositeTestRunFilter(
                listOf(
                    AnnotatedWithFilter(listOf("Run.Me")),
                    IgnoredAnnotationFilter(setOf("Dont.Run.Me"))
                )
            )
        )

        assertThat(verdict).isInstanceOf<TestRunFilter.Verdict.Skip.NotAnnotatedWith>()
    }

    private fun checkIfRunNeeded(filter: CompositeTestRunFilter): TestRunFilter.Verdict {
        return filter.runNeeded(
            test = TestInApk.createStubInstance(
                annotations = listOf(AnnotationData("Dont.Run.Me", emptyMap()))
            ),
            deviceName = DeviceName("api22"),
            api = 22
        )
    }
}
