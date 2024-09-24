package com.avito.runner.scheduler.suite.filter

import com.avito.runner.scheduler.suite.filter.TestsFilter.Result.Included
import com.avito.truth.isInstanceOf
import com.google.common.truth.Truth.assertWithMessage
import org.junit.jupiter.api.Test

internal class ImpactAnalysisFilterFactoryTest {

    private val test1 = "com.test.Test1"
    private val test2 = "com.test.Test2"
    private val test3 = "com.test.Test3"
    private val test4 = "com.test.Test4"

    @Test
    fun `run all - filters nothing`() {
        val filter = StubFilterFactoryFactory.create(
            impactAnalysisResult = ImpactAnalysisResult.createStubInstance(
                mode = ImpactAnalysisMode.ALL
            )
        ).createFilter()

        filter.assertIncluded(test1, test2, test3, test4)
    }

    @Test
    fun `run all - filters nothing - even if impact provided`() {
        val filter = StubFilterFactoryFactory.create(
            impactAnalysisResult = ImpactAnalysisResult.createStubInstance(
                mode = ImpactAnalysisMode.ALL
            )
        ).createFilter()

        filter.assertIncluded(test1, test2, test3, test4)
    }

    @Test
    fun `run all except changed - filtered impacted tests`() {
        val filter = StubFilterFactoryFactory.create(
            impactAnalysisResult = ImpactAnalysisResult.createStubInstance(
                mode = ImpactAnalysisMode.ALL_EXCEPT_CHANGED,
                changedTests = listOf(test1)
            )
        ).createFilter()

        filter.assertIncluded(test2, test3, test4)
    }

    @Test
    fun `run all and changed is not empty - filter is empty`() {
        val filter = StubFilterFactoryFactory.create(
            impactAnalysisResult = ImpactAnalysisResult.createStubInstance(
                mode = ImpactAnalysisMode.ALL,
                changedTests = listOf(test1)
            )
        ).createFilter()

        filter.assertIncluded(test1, test2, test3, test4)
    }

    @Test
    fun `run changed - filtered not impacted tests`() {
        val filter = StubFilterFactoryFactory.create(
            impactAnalysisResult = ImpactAnalysisResult.createStubInstance(
                mode = ImpactAnalysisMode.CHANGED,
                changedTests = listOf(test1)
            )
        ).createFilter()

        filter.assertIncluded(test1)
    }

    private fun TestsFilter.assertIncluded(vararg name: String) {
        name.forEach {
            val result = filter(TestsFilter.Test.createStub(it))
            assertWithMessage(it).that(result).isInstanceOf<Included>()
        }
    }
}
