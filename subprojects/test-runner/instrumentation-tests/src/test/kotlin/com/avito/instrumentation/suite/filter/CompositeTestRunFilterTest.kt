package com.avito.instrumentation.suite.filter

import com.avito.instrumentation.createStub
import com.avito.instrumentation.internal.suite.filter.CompositionFilter
import com.avito.instrumentation.internal.suite.filter.TestsFilter
import com.avito.instrumentation.internal.suite.filter.TestsFilter.Result.Excluded
import com.avito.truth.isInstanceOf
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class CompositeTestRunFilterTest {

    @Test
    fun `exclude reason must be returned by first applied exclude filter`() {
        val compositionFilter = CompositionFilter(
            listOf(
                object : TestsFilter {
                    override val name: String
                        get() = TODO("Not yet implemented")

                    override fun filter(test: TestsFilter.Test): TestsFilter.Result {
                        return Excluded.HasSkipSdkAnnotation("", 21)
                    }
                },
                object : TestsFilter {
                    override val name: String
                        get() = TODO("Not yet implemented")

                    override fun filter(test: TestsFilter.Test): TestsFilter.Result {
                        return Excluded.HasExcludeAnnotations("", emptySet())
                    }
                }
            )
        )

        assertThat(compositionFilter.filter(TestsFilter.Test.createStub()))
            .isInstanceOf<Excluded.HasSkipSdkAnnotation>()
    }

    @Test
    fun `if any one of filters return exclude then test will be excluded`() {
        val compositionFilter = CompositionFilter(
            listOf(
                object : TestsFilter {
                    override val name: String = ""

                    override fun filter(test: TestsFilter.Test): TestsFilter.Result {
                        return TestsFilter.Result.Included
                    }
                },
                object : TestsFilter {
                    override val name: String = ""

                    override fun filter(test: TestsFilter.Test): TestsFilter.Result {
                        return Excluded.HasExcludeAnnotations("", emptySet())
                    }
                }
            )
        )

        assertThat(compositionFilter.filter(TestsFilter.Test.createStub()))
            .isInstanceOf<Excluded>()
    }
}
