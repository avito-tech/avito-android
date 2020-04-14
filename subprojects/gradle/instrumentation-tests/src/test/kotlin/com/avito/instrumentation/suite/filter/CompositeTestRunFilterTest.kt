package com.avito.instrumentation.suite.filter

import com.avito.instrumentation.createStub
import com.avito.instrumentation.suite.filter.TestsFilter.Result.Excluded
import com.google.common.truth.Truth
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
                        return Excluded.HaveSkipSdkAnnotation(21)
                    }
                },
                object : TestsFilter {
                    override val name: String
                        get() = TODO("Not yet implemented")

                    override fun filter(test: TestsFilter.Test): TestsFilter.Result {
                        return Excluded.HaveExcludeAnnotations(emptySet())
                    }
                }
            )
        )

        Truth.assertThat(compositionFilter.filter(TestsFilter.Test.createStub()))
            .isInstanceOf(Excluded.HaveSkipSdkAnnotation::class.java)
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
                        return Excluded.HaveExcludeAnnotations(emptySet())
                    }
                }
            )
        )

        Truth.assertThat(compositionFilter.filter(TestsFilter.Test.createStub()))
            .isInstanceOf(Excluded::class.java)
    }
}
