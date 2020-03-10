package com.avito.android.runner.annotation.resolver

import com.avito.android.test.annotations.Behavior
import com.avito.android.test.annotations.CaseId
import com.avito.android.test.annotations.DataSetNumber
import com.avito.android.test.annotations.Description
import com.avito.android.test.annotations.ExternalId
import com.avito.android.test.annotations.FeatureId
import com.avito.android.test.annotations.Flaky
import com.avito.android.test.annotations.Kind
import com.avito.android.test.annotations.Priority
import com.avito.android.test.annotations.TagId
import com.avito.android.test.annotations.TestCaseBehavior
import com.avito.android.test.annotations.TestCasePriority
import com.avito.android.test.report.model.TestMetadata
import com.avito.report.model.Flakiness
import com.avito.report.model.TestKind
import java.lang.reflect.Method

class TestMetadataAnnotationResolver : TestMetadataResolver {

    override val key: String = TEST_METADATA_KEY

    override fun resolve(test: String): TestMetadataResolver.Resolution {
        val subset = arrayOf(
            FeatureId::class.java,
            Description::class.java,
            DataSetNumber::class.java,
            Priority::class.java,
            Behavior::class.java,
            ExternalId::class.java,
            TagId::class.java,
            Flaky::class.java,
            Kind::class.java
        )

        var testClass: Class<*>? = null
        var method: Method? = null
        val methodResolution = MethodStringRepresentation.parseString(test)

        val annotationsExtractingResult = when (methodResolution) {

            is MethodStringRepresentation.Resolution.ClassOnly -> {
                testClass = methodResolution.aClass
                Annotations.getAnnotationsSubset(
                    methodResolution.aClass,
                    null,
                    *subset
                )
            }

            is MethodStringRepresentation.Resolution.Method -> {
                testClass = methodResolution.aClass
                method = methodResolution.method
                Annotations.getAnnotationsSubset(
                    methodResolution.aClass,
                    methodResolution.method,
                    *subset
                )
            }

            is MethodStringRepresentation.Resolution.ParseError -> {
                throw RuntimeException("Failed to parse annotations from $test")
            }
        }

        var kind: TestKind = TestKind.UNKNOWN
        var caseId: Int? = null
        var description: String? = null
        var priority: TestCasePriority? = null
        var behavior: TestCaseBehavior? = null
        var dataSetNumber: Int? = null
        val testMethod: Method? = method
        var externalId: String? = null
        var tagIds: List<Int> = emptyList()
        var featureIds: List<Int> = emptyList()
        var flakiness: Flakiness = Flakiness.Stable

        annotationsExtractingResult
            .forEach { annotation ->
                when (annotation) {
                    is Kind -> {
                        kind = annotation.value
                    }
                    is CaseId -> {
                        caseId = annotation.value
                    }
                    is Description -> {
                        description = annotation.value
                    }
                    is Priority -> {
                        priority = annotation.priority
                    }
                    is Behavior -> {
                        behavior = annotation.behavior
                    }
                    is DataSetNumber -> {
                        dataSetNumber = annotation.value
                    }
                    is ExternalId -> {
                        externalId = annotation.value
                    }
                    is FeatureId -> {
                        featureIds = annotation.value.toList()
                    }
                    is TagId -> {
                        tagIds = annotation.value.toList()
                    }
                    is Flaky -> {
                        flakiness = Flakiness.Flaky(annotation.reason)
                    }
                }
            }

        return TestMetadataResolver.Resolution.ReplaceSerializable(
            replacement = TestMetadata(
                caseId = caseId,
                description = description,
                className = testClass.name,
                methodName = testMethod?.name,
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
