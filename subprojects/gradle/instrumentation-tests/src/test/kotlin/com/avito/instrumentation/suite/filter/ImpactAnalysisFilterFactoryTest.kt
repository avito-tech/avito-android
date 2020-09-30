package com.avito.instrumentation.suite.filter

import com.avito.instrumentation.configuration.ImpactAnalysisPolicy
import com.avito.instrumentation.createStub
import com.avito.instrumentation.suite.filter.TestsFilter.Result.Included
import com.google.common.truth.Truth.assertWithMessage
import com.google.common.truth.isInstanceOf
import com.google.common.truth.isNotInstanceOf
import org.junit.jupiter.api.Test

internal class ImpactAnalysisFilterFactoryTest {

    private val test1 = "com.test.Test1"
    private val test2 = "com.test.Test2"
    private val test3 = "com.test.Test3"
    private val test4 = "com.test.Test4"

    @Test
    fun `noImpactAnalysis - filters nothing`() {
        val filter = FilterFactoryFactory.create(
            impactAnalysisResult = ImpactAnalysisResult(
                policy = ImpactAnalysisPolicy.Off,
                affectedTests = emptyList(),
                addedTests = emptyList(),
                modifiedTests = emptyList()
            )
        ).createFilter()

        filter.assertIncluded(test1, test2, test3, test4)
    }

    @Test
    fun `noImpactAnalysis - filters nothing - even if impact provided`() {
        val filter = FilterFactoryFactory.create(
            impactAnalysisResult = ImpactAnalysisResult(
                policy = ImpactAnalysisPolicy.Off,
                affectedTests = listOf(test1, test2, test3, test4),
                addedTests = listOf(test1, test2, test3, test4),
                modifiedTests = listOf(test1, test2, test3, test4)
            )
        ).createFilter()

        filter.assertIncluded(test1, test2, test3, test4)
    }

    @Test
    fun `runAffectedTests - filters out new and modified`() {
        val filter = FilterFactoryFactory.create(
            impactAnalysisResult = ImpactAnalysisResult(
                policy = ImpactAnalysisPolicy.On.RunAffectedTests,
                affectedTests = listOf(test1, test2, test3, test4),
                addedTests = listOf(test2),
                modifiedTests = listOf(test3)
            )
        ).createFilter()

        filter.assertIncluded(test1, test4)
        filter.assertNotIncluded(test2, test3)
    }

    @Test
    fun `runAffectedTests - filters nothing - no added or modified`() {
        val filter = FilterFactoryFactory.create(
            impactAnalysisResult = ImpactAnalysisResult(
                policy = ImpactAnalysisPolicy.On.RunAffectedTests,
                affectedTests = listOf(test1, test2, test3, test4),
                addedTests = emptyList(),
                modifiedTests = emptyList()
            )
        ).createFilter()

        filter.assertIncluded(test1, test2, test3, test4)
    }

    @Test
    fun `runAffectedTests - filters nothing - no affected`() {
        val filter = FilterFactoryFactory.create(
            impactAnalysisResult = ImpactAnalysisResult(
                policy = ImpactAnalysisPolicy.On.RunAffectedTests,
                affectedTests = emptyList(),
                addedTests = emptyList(),
                modifiedTests = emptyList()
            )
        ).createFilter()

        filter.assertNotIncluded(test1, test2, test3, test4)
    }

    @Test
    fun `runAffectedTests - filters nothing - new and modified provided`() {
        val filter = FilterFactoryFactory.create(
            impactAnalysisResult = ImpactAnalysisResult(
                policy = ImpactAnalysisPolicy.On.RunAffectedTests,
                affectedTests = emptyList(),
                addedTests = listOf(test2),
                modifiedTests = listOf(test3)
            )
        ).createFilter()

        filter.assertNotIncluded(test1, test2, test3, test4)
    }

    @Test
    fun `runNewTests - filters out all - affected provided`() {
        val filter = FilterFactoryFactory.create(
            impactAnalysisResult = ImpactAnalysisResult(
                policy = ImpactAnalysisPolicy.On.RunNewTests,
                affectedTests = listOf(test1, test2, test3, test4),
                addedTests = emptyList(),
                modifiedTests = emptyList()
            )
        ).createFilter()

        filter.assertNotIncluded(test1, test2, test3, test4)
    }

    @Test
    fun `runNewTests - filters in only added - affected provided`() {
        val filter = FilterFactoryFactory.create(
            impactAnalysisResult = ImpactAnalysisResult(
                policy = ImpactAnalysisPolicy.On.RunNewTests,
                affectedTests = listOf(test1, test2, test3, test4),
                addedTests = listOf(test2, test4),
                modifiedTests = emptyList()
            )
        ).createFilter()

        filter.assertIncluded(test2, test4)
        filter.assertNotIncluded(test1, test3)
    }

    @Test
    fun `runNewTests - filters in only added - affected and modified provided`() {
        val filter = FilterFactoryFactory.create(
            impactAnalysisResult = ImpactAnalysisResult(
                policy = ImpactAnalysisPolicy.On.RunNewTests,
                affectedTests = listOf(test1, test2, test3, test4),
                addedTests = listOf(test2, test4),
                modifiedTests = listOf(test3)
            )
        ).createFilter()

        filter.assertIncluded(test2, test4)
        filter.assertNotIncluded(test1, test3)
    }

    @Test
    fun `runModifiedTests - filters in only modified - affected provided`() {
        val filter = FilterFactoryFactory.create(
            impactAnalysisResult = ImpactAnalysisResult(
                policy = ImpactAnalysisPolicy.On.RunModifiedTests,
                affectedTests = listOf(test1, test2, test3, test4),
                addedTests = emptyList(),
                modifiedTests = listOf(test1, test3)
            )
        ).createFilter()

        filter.assertIncluded(test1, test3)
        filter.assertNotIncluded(test2, test4)
    }

    @Test
    fun `runModifiedTests - filters out all - no modified provided`() {
        val filter = FilterFactoryFactory.create(
            impactAnalysisResult = ImpactAnalysisResult(
                policy = ImpactAnalysisPolicy.On.RunModifiedTests,
                affectedTests = listOf(test1, test2, test3, test4),
                addedTests = listOf(test1, test3),
                modifiedTests = emptyList()
            )
        ).createFilter()

        filter.assertNotIncluded(test1, test2, test3, test4)
    }

    private fun TestsFilter.assertIncluded(vararg name: String) {
        name.forEach {
            val result = filter(TestsFilter.Test.createStub(it))
            assertWithMessage(it).that(result).isInstanceOf<Included>()
        }
    }

    private fun TestsFilter.assertNotIncluded(vararg name: String) {
        name.forEach {
            val result = filter(TestsFilter.Test.createStub(it))
            assertWithMessage(it).that(result).isNotInstanceOf<Included>()
        }
    }
}
