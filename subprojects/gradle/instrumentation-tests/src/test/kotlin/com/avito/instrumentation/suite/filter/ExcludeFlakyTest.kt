package com.avito.instrumentation.suite.filter

import com.avito.instrumentation.configuration.InstrumentationFilter
import com.avito.instrumentation.createStub
import com.avito.report.model.Flakiness
import com.avito.truth.isInstanceOf
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class ExcludeFlakyTest {

    @Test
    fun include_stable() {
        val result = ExcludeByFlakyFilter().filter(
            TestsFilter.Test.createStub(
                flakiness = Flakiness.Stable
            )
        )

        assertThat(result)
            .isEqualTo(TestsFilter.Result.Included)
    }

    @Test
    fun exclude_flaky() {
        val result = ExcludeByFlakyFilter().filter(
            TestsFilter.Test.createStub(
                flakiness = Flakiness.Flaky("")
            )
        )

        assertThat(result).isInstanceOf<TestsFilter.Result.Excluded.HasFlakyAnnotation>()
    }

    @Test
    fun exclude_flaky_in_integration_with_filter_factory() {
        val filterFactory = FilterFactoryFactory.create(
            filter = InstrumentationFilter.Data.createStub(
                excludeFlaky = true
            )
        )

        val result = filterFactory.createFilter().filter(
            TestsFilter.Test.createStub(
                flakiness = Flakiness.Flaky("")
            )
        )

        assertThat(result).isInstanceOf<TestsFilter.Result.Excluded.HasFlakyAnnotation>()
    }

    @Test
    fun include_stable_in_integration_with_filter_factory() {
        val filterFactory = FilterFactoryFactory.create(
            filter = InstrumentationFilter.Data.createStub(
                excludeFlaky = true
            )
        )

        val result = filterFactory.createFilter().filter(
            TestsFilter.Test.createStub(
                flakiness = Flakiness.Stable
            )
        )

        assertThat(result)
            .isEqualTo(TestsFilter.Result.Included)
    }

    @Test
    fun include_flaky_in_integration_with_filter_factory() {
        val filterFactory = FilterFactoryFactory.create(
            filter = InstrumentationFilter.Data.createStub(
                excludeFlaky = false
            )
        )
        val result = filterFactory.createFilter().filter(
            TestsFilter.Test.createStub(
                flakiness = Flakiness.Flaky("")
            )
        )

        assertThat(result)
            .isEqualTo(TestsFilter.Result.Included)
    }
}
