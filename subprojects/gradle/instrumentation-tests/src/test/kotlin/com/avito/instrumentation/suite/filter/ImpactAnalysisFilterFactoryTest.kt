package com.avito.instrumentation.suite.filter

import com.avito.instrumentation.configuration.ImpactAnalysisPolicy
import com.avito.instrumentation.createStub
import com.avito.instrumentation.createStubInstance
import com.avito.instrumentation.internal.suite.filter.ImpactAnalysisResult
import com.avito.instrumentation.internal.suite.filter.TestsFilter
import com.avito.instrumentation.internal.suite.filter.TestsFilter.Result.Included
import com.avito.truth.isInstanceOf
import com.google.common.truth.Truth.assertWithMessage
import org.junit.jupiter.api.Test

internal class ImpactAnalysisFilterFactoryTest {

    private val test1 = "com.test.Test1"
    private val test2 = "com.test.Test2"
    private val test3 = "com.test.Test3"
    private val test4 = "com.test.Test4"

    @Test
    fun `noImpactAnalysis - filters nothing`() {
        val filter = FilterFactoryFactory.create(
            impactAnalysisResult = ImpactAnalysisResult.createStubInstance(
                policy = ImpactAnalysisPolicy.Off
            )
        ).createFilter()

        filter.assertIncluded(test1, test2, test3, test4)
    }

    @Test
    fun `noImpactAnalysis - filters nothing - even if impact provided`() {
        val filter = FilterFactoryFactory.create(
            impactAnalysisResult = ImpactAnalysisResult.createStubInstance(
                policy = ImpactAnalysisPolicy.Off,
                affectedTests = listOf(test1, test2, test3, test4),
                addedTests = listOf(test1, test2, test3, test4),
                modifiedTests = listOf(test1, test2, test3, test4)
            )
        ).createFilter()

        filter.assertIncluded(test1, test2, test3, test4)
    }

    private fun TestsFilter.assertIncluded(vararg name: String) {
        name.forEach {
            val result = filter(TestsFilter.Test.createStub(it))
            assertWithMessage(it).that(result).isInstanceOf<Included>()
        }
    }
}
