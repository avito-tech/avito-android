package com.avito.android.runner.annotation.resolver

import android.os.Build
import com.avito.android.test.annotations.Behavior
import com.avito.android.test.annotations.CaseId
import com.avito.android.test.annotations.DataSetNumber
import com.avito.android.test.annotations.Description
import com.avito.android.test.annotations.E2EStub
import com.avito.android.test.annotations.E2ETest
import com.avito.android.test.annotations.ExternalId
import com.avito.android.test.annotations.FeatureId
import com.avito.android.test.annotations.Flaky
import com.avito.android.test.annotations.IntegrationTest
import com.avito.android.test.annotations.ManualTest
import com.avito.android.test.annotations.Priority
import com.avito.android.test.annotations.ScreenshotTest
import com.avito.android.test.annotations.SyntheticMonitoringTest
import com.avito.android.test.annotations.TagId
import com.avito.android.test.annotations.TestCaseBehavior
import com.avito.android.test.annotations.TestCasePriority
import com.avito.android.test.annotations.UIComponentStub
import com.avito.android.test.annotations.UIComponentTest
import com.avito.android.test.annotations.UnitTest
import com.avito.android.test.report.model.TestMetadata
import com.avito.report.model.Flakiness
import com.avito.report.model.Kind

class TestMetadataAnnotationResolver : TestMetadataResolver {

    override val key: String = TEST_METADATA_KEY

    override fun resolve(test: String): TestMetadataResolver.Resolution {
        val testOrClass = MethodStringRepresentation.parseString(test).getTestOrThrow()

        val kind: Kind = TestKindExtractor.extract(testOrClass)
        var caseId: Int? = null
        var description: String? = null
        var priority: TestCasePriority? = null
        var behavior: TestCaseBehavior? = null
        var dataSetNumber: Int? = null
        var externalId: String? = null
        var tagIds: List<Int> = emptyList()
        var featureIds: List<Int> = emptyList()
        var flakiness: Flakiness = Flakiness.Stable

        val annotationTypes = arrayOf(
            FeatureId::class.java,
            Description::class.java,
            DataSetNumber::class.java,
            Priority::class.java,
            Behavior::class.java,
            ExternalId::class.java,
            TagId::class.java,
            Flaky::class.java,
            UIComponentTest::class.java,
            E2ETest::class.java,
            IntegrationTest::class.java,
            ManualTest::class.java,
            UIComponentStub::class.java,
            E2EStub::class.java,
            UnitTest::class.java,
            ScreenshotTest::class.java,
            SyntheticMonitoringTest::class.java
        )

        val testAnnotations = Annotations.getAnnotationsSubset(
            testOrClass.testClass,
            testOrClass.testMethod,
            *annotationTypes
        )

        testAnnotations
            .forEach { annotation ->
                when (annotation) {
                    is CaseId -> caseId = annotation.value
                    is Description -> description = annotation.value
                    is Priority -> priority = annotation.priority
                    is Behavior -> behavior = annotation.behavior
                    is DataSetNumber -> dataSetNumber = annotation.value
                    is ExternalId -> externalId = annotation.value
                    is FeatureId -> featureIds = annotation.value.toList()
                    is TagId -> tagIds = annotation.value.toList()
                    is Flaky -> flakiness = when {
                        annotation.onSdks.isEmpty() || annotation.onSdks.contains(Build.VERSION.SDK_INT) ->
                            Flakiness.Flaky(annotation.reason)
                        else ->
                            Flakiness.Stable
                    }
                }
            }

        return TestMetadataResolver.Resolution.ReplaceSerializable(
            replacement = TestMetadata(
                caseId = caseId,
                description = description,
                className = testOrClass.testClass.name,
                methodName = testOrClass.testMethod?.name,
                dataSetNumber = dataSetNumber,
                kind = kind,
                priority = priority,
                behavior = behavior,
                externalId = externalId,
                featureIds = featureIds,
                tagIds = tagIds,
                flakiness = flakiness
            )
        )
    }
}

const val TEST_METADATA_KEY = "testMetadata"
