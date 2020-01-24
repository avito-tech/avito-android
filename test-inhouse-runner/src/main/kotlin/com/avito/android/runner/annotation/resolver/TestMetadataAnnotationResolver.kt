package com.avito.android.runner.annotation.resolver

import com.avito.android.test.annotations.Behavior
import com.avito.android.test.annotations.CaseId
import com.avito.android.test.annotations.ComponentTest
import com.avito.android.test.annotations.DataSetNumber
import com.avito.android.test.annotations.Description
import com.avito.android.test.annotations.ExternalId
import com.avito.android.test.annotations.Feature
import com.avito.android.test.annotations.FunctionalTest
import com.avito.android.test.annotations.InstrumentationUnitTest
import com.avito.android.test.annotations.ManualTest
import com.avito.android.test.annotations.MessengerTest
import com.avito.android.test.annotations.PerformanceComponentTest
import com.avito.android.test.annotations.PerformanceFunctionalTest
import com.avito.android.test.annotations.Priority
import com.avito.android.test.annotations.PublishTest
import com.avito.android.test.annotations.ScreenshotTest
import com.avito.android.test.annotations.TagId
import com.avito.android.test.annotations.TestCaseBehavior
import com.avito.android.test.annotations.TestCasePriority
import com.avito.android.test.report.TestPackageParser
import com.avito.android.test.report.model.TestMetadata
import com.avito.android.test.report.model.TestType
import com.avito.report.model.Kind
import java.lang.reflect.Method

class TestMetadataAnnotationResolver(
    private val testPackageParser: TestPackageParser = TestPackageParser.Impl()
) : TestMetadataResolver {

    override val key: String = TEST_METADATA_KEY

    override fun resolve(test: String): TestMetadataResolver.Resolution {
        val methodResolution = MethodStringRepresentation.parseString(test)

        val subset = arrayOf(
            FunctionalTest::class.java,
            PerformanceFunctionalTest::class.java,
            PerformanceComponentTest::class.java,
            ScreenshotTest::class.java,
            ComponentTest::class.java,
            PublishTest::class.java,
            MessengerTest::class.java,
            ManualTest::class.java,
            InstrumentationUnitTest::class.java,
            CaseId::class.java,
            Description::class.java,
            DataSetNumber::class.java,
            Priority::class.java,
            Behavior::class.java,
            Feature::class.java,
            ExternalId::class.java,
            TagId::class.java
        )

        var testClass: Class<*>? = null
        var method: Method? = null

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

        var testType: TestType = TestType.NONE
        var kind: Kind = Kind.UNKNOWN
        var caseId: Int? = null
        var description: String? = null
        var priority: TestCasePriority? = null
        var behavior: TestCaseBehavior? = null
        var features: List<String> = emptyList()
        var dataSetNumber: Int? = null
        val testMethod: Method? = method
        var externalId: String? = null
        var tagIds: List<Int> = emptyList()

        annotationsExtractingResult
            .forEach { annotation ->
                when (annotation) {
                    is FunctionalTest -> {
                        testType = TestType.FUNCTIONAL
                        kind = Kind.E2E
                    }
                    is PerformanceFunctionalTest -> {
                        testType = TestType.PERFORMANCE_FUNCTIONAL
                        kind = Kind.E2E
                    }
                    is PerformanceComponentTest -> {
                        testType = TestType.PERFORMANCE_COMPONENT
                        kind = Kind.UI_COMPONENT
                    }
                    is ScreenshotTest -> {
                        testType = TestType.SCREENSHOT
                        kind = Kind.UI_COMPONENT
                    }
                    is ComponentTest -> {
                        testType = TestType.COMPONENT
                        kind = Kind.UI_COMPONENT
                    }
                    is PublishTest -> {
                        testType = TestType.PUBLISH
                        kind = Kind.UI_COMPONENT
                    }
                    is MessengerTest -> {
                        testType = TestType.MESSENGER
                        kind = Kind.UI_COMPONENT
                    }
                    is ManualTest -> {
                        testType = TestType.MANUAL
                        kind = Kind.MANUAL
                    }
                    is InstrumentationUnitTest -> {
                        testType = TestType.UNIT
                        kind = Kind.INTEGRATION
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
                    is Feature -> {
                        features = annotation.value.toList()
                    }
                    is DataSetNumber -> {
                        dataSetNumber = annotation.value
                    }
                    is ExternalId -> {
                        externalId = annotation.value
                    }
                    is TagId -> {
                        tagIds = annotation.value.toList()
                    }
                }
            }

        return TestMetadataResolver.Resolution.ReplaceSerializable(
            replacement = TestMetadata(
                caseId = caseId,
                description = description,
                testType = testType,
                kind = kind,
                priority = priority,
                behavior = behavior,
                dataSetNumber = dataSetNumber,
                features = features,
                className = testClass.name,
                methodName = testMethod?.name,
                packageParserResult = testPackageParser.parse(testClass.`package`?.name),
                externalId = externalId,
                tagIds = tagIds
            )
        )
    }
}

const val TEST_METADATA_KEY = "testMetadata"
