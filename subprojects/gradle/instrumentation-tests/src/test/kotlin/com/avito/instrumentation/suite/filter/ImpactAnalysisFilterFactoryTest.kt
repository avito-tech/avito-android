package com.avito.instrumentation.suite.filter

import com.avito.instrumentation.configuration.ImpactAnalysisPolicy
import com.avito.instrumentation.createStub
import com.avito.instrumentation.suite.filter.TestsFilter.Result.Included
import com.google.common.truth.Truth.assertWithMessage
import com.google.common.truth.isInstanceOf
import com.google.common.truth.isNotInstanceOf
import org.junit.jupiter.api.Test

internal class ImpactAnalysisFilterFactoryTest {

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

        filter.assertIncluded("com.test.Test1", "com.test.Test2", "com.test.Test3", "com.test.Test4")
    }

    @Test
    fun `noImpactAnalysis - filters nothing - even if impact provided`() {
        val filter = FilterFactoryFactory.create(
            impactAnalysisResult = ImpactAnalysisResult(
                policy = ImpactAnalysisPolicy.Off,
                affectedTests = listOf("com.test.Test1", "com.test.Test2", "com.test.Test3", "com.test.Test4"),
                addedTests = listOf("com.test.Test1", "com.test.Test2", "com.test.Test3", "com.test.Test4"),
                modifiedTests = listOf("com.test.Test1", "com.test.Test2", "com.test.Test3", "com.test.Test4")
            )
        ).createFilter()

        filter.assertIncluded("com.test.Test1", "com.test.Test2", "com.test.Test3", "com.test.Test4")
    }

    @Test
    fun `runAffectedTests - filters out new and modified`() {
        val filter = FilterFactoryFactory.create(
            impactAnalysisResult = ImpactAnalysisResult(
                policy = ImpactAnalysisPolicy.On.RunAffectedTests,
                affectedTests = listOf("com.test.Test1", "com.test.Test2", "com.test.Test3", "com.test.Test4"),
                addedTests = listOf("com.test.Test2"),
                modifiedTests = listOf("com.test.Test3")
            )
        ).createFilter()

        filter.assertIncluded("com.test.Test1", "com.test.Test4")
        filter.assertNotIncluded("com.test.Test2", "com.test.Test3")
    }

    @Test
    fun `runAffectedTests - filters nothing - no added or modified`() {
        val filter = FilterFactoryFactory.create(
            impactAnalysisResult = ImpactAnalysisResult(
                policy = ImpactAnalysisPolicy.On.RunAffectedTests,
                affectedTests = listOf("com.test.Test1", "com.test.Test2", "com.test.Test3", "com.test.Test4"),
                addedTests = emptyList(),
                modifiedTests = emptyList()
            )
        ).createFilter()

        filter.assertIncluded("com.test.Test1", "com.test.Test2", "com.test.Test3", "com.test.Test4")
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

        filter.assertNotIncluded("com.test.Test1", "com.test.Test2", "com.test.Test3", "com.test.Test4")
    }

    @Test
    fun `runAffectedTests - filters nothing - new and modified provided`() {
        val filter = FilterFactoryFactory.create(
            impactAnalysisResult = ImpactAnalysisResult(
                policy = ImpactAnalysisPolicy.On.RunAffectedTests,
                affectedTests = emptyList(),
                addedTests = listOf("com.test.Test2"),
                modifiedTests = listOf("com.test.Test3")
            )
        ).createFilter()

        filter.assertNotIncluded("com.test.Test1", "com.test.Test2", "com.test.Test3", "com.test.Test4")
    }

    @Test
    fun `runNewTests - filters out all - affected provided`() {
        val filter = FilterFactoryFactory.create(
            impactAnalysisResult = ImpactAnalysisResult(
                policy = ImpactAnalysisPolicy.On.RunNewTests,
                affectedTests = listOf("com.test.Test1", "com.test.Test2", "com.test.Test3", "com.test.Test4"),
                addedTests = emptyList(),
                modifiedTests = emptyList()
            )
        ).createFilter()

        filter.assertNotIncluded("com.test.Test1", "com.test.Test2", "com.test.Test3", "com.test.Test4")
    }

    @Test
    fun `runNewTests - filters in only added - affected provided`() {
        val filter = FilterFactoryFactory.create(
            impactAnalysisResult = ImpactAnalysisResult(
                policy = ImpactAnalysisPolicy.On.RunNewTests,
                affectedTests = listOf("com.test.Test1", "com.test.Test2", "com.test.Test3", "com.test.Test4"),
                addedTests = listOf("com.test.Test2", "com.test.Test4"),
                modifiedTests = emptyList()
            )
        ).createFilter()

        filter.assertIncluded("com.test.Test2", "com.test.Test4")
        filter.assertNotIncluded("com.test.Test1", "com.test.Test3")
    }

    @Test
    fun `runNewTests - filters in only added - affected and modified provided`() {
        val filter = FilterFactoryFactory.create(
            impactAnalysisResult = ImpactAnalysisResult(
                policy = ImpactAnalysisPolicy.On.RunNewTests,
                affectedTests = listOf("com.test.Test1", "com.test.Test2", "com.test.Test3", "com.test.Test4"),
                addedTests = listOf("com.test.Test2", "com.test.Test4"),
                modifiedTests = listOf("com.test.Test3")
            )
        ).createFilter()

        filter.assertIncluded("com.test.Test2", "com.test.Test4")
        filter.assertNotIncluded("com.test.Test1", "com.test.Test3")
    }

    @Test
    fun `runModifiedTests - filters in only modified - affected provided`() {
        val filter = FilterFactoryFactory.create(
            impactAnalysisResult = ImpactAnalysisResult(
                policy = ImpactAnalysisPolicy.On.RunModifiedTests,
                affectedTests = listOf("com.test.Test1", "com.test.Test2", "com.test.Test3", "com.test.Test4"),
                addedTests = emptyList(),
                modifiedTests = listOf("com.test.Test1", "com.test.Test3")
            )
        ).createFilter()

        filter.assertIncluded("com.test.Test1", "com.test.Test3")
        filter.assertNotIncluded("com.test.Test2", "com.test.Test4")
    }

    @Test
    fun `runModifiedTests - filters out all - no modified provided`() {
        val filter = FilterFactoryFactory.create(
            impactAnalysisResult = ImpactAnalysisResult(
                policy = ImpactAnalysisPolicy.On.RunModifiedTests,
                affectedTests = listOf("com.test.Test1", "com.test.Test2", "com.test.Test3", "com.test.Test4"),
                addedTests = listOf("com.test.Test1", "com.test.Test3"),
                modifiedTests = emptyList()
            )
        ).createFilter()

        filter.assertNotIncluded("com.test.Test1", "com.test.Test2", "com.test.Test3", "com.test.Test4")
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
