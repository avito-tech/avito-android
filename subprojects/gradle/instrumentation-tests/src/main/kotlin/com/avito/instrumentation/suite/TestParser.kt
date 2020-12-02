package com.avito.instrumentation.suite

import com.avito.android.AnnotationData
import com.avito.android.TestInApk
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
import com.avito.android.test.annotations.NO_REASON
import com.avito.android.test.annotations.PerformanceComponentTest
import com.avito.android.test.annotations.PerformanceFunctionalTest
import com.avito.android.test.annotations.Priority
import com.avito.android.test.annotations.ScreenshotTest
import com.avito.android.test.annotations.TagId
import com.avito.android.test.annotations.TestCaseBehavior
import com.avito.android.test.annotations.TestCasePriority
import com.avito.android.test.annotations.UIComponentStub
import com.avito.android.test.annotations.UIComponentTest
import com.avito.android.test.annotations.UnitTest
import com.avito.report.model.DeviceName
import com.avito.report.model.Flakiness
import com.avito.report.model.Kind
import com.avito.report.model.TestStaticData
import com.avito.report.model.TestStaticDataPackage

internal fun parseTest(
    testInApk: TestInApk,
    deviceName: DeviceName,
    deviceApi: Int
): TestStaticData = TestStaticDataPackage(
    name = testInApk.testName,

    device = deviceName,

    description = testInApk.annotations
        .find { it.name == Description::class.java.canonicalName }
        ?.getStringValue(DESCRIPTION_VALUE_KEY),

    testCaseId = testInApk.annotations
        .find { it.name == CaseId::class.java.canonicalName }
        ?.getIntValue(TEST_CASE_ID_VALUE_KEY),

    dataSetNumber = testInApk.annotations
        .find { it.name == DataSetNumber::class.java.canonicalName }
        ?.getIntValue(DATA_SET_NUMBER_VALUE_KEY),

    externalId = testInApk.annotations
        .find { it.name == ExternalId::class.java.canonicalName }
        ?.getStringValue(EXTERNAL_ID_VALUE_KEY),

    tagIds = testInApk.annotations
        .find { it.name == TagId::class.java.canonicalName }
        ?.getIntArrayValue(TAG_ID_VALUE_KEY) ?: emptyList(),

    featureIds = testInApk.annotations
        .find { it.name == FeatureId::class.java.canonicalName }
        ?.getIntArrayValue(FEATURE_ID_VALUE_KEY) ?: emptyList(),

    priority = testInApk.annotations
        .find { it.name == Priority::class.java.name }
        ?.getEnumValue("priority")?.let { TestCasePriority.fromName(it) },

    behavior = testInApk.annotations
        .find { it.name == Behavior::class.java.name }
        ?.getEnumValue("behavior")?.let { TestCaseBehavior.fromName(it) },

    kind = determineKind(testInApk.annotations),

    flakiness = determineFlakiness(testInApk.annotations, deviceApi)
)

private val annotationsToKindMap = mapOf(
    ManualTest::class.java.canonicalName to Kind.MANUAL,
    PerformanceFunctionalTest::class.java.canonicalName to Kind.E2E,
    PerformanceComponentTest::class.java.canonicalName to Kind.UI_COMPONENT,
    ScreenshotTest::class.java.canonicalName to Kind.UI_COMPONENT,
    E2EStub::class.java.canonicalName to Kind.E2E_STUB,
    E2ETest::class.java.canonicalName to Kind.E2E,
    UIComponentTest::class.java.canonicalName to Kind.UI_COMPONENT,
    IntegrationTest::class.java.canonicalName to Kind.INTEGRATION,
    UIComponentStub::class.java.canonicalName to Kind.UI_COMPONENT_STUB,
    UnitTest::class.java.canonicalName to Kind.UNIT
)

private fun determineFlakiness(annotations: List<AnnotationData>, api: Int): Flakiness {
    val flakyAnnotation = annotations.find { it.name == Flaky::class.java.canonicalName }
    return when {
        flakyAnnotation != null -> {
            val flakySdks = flakyAnnotation.getIntArrayValue(FLAKY_SDKS_KEY)
            // by defaul vararg parameter is initialized by emptyArray
            val isFlaky = if (flakySdks != null && flakySdks.isNotEmpty()) {
                flakySdks.contains(api)
            } else {
                true
            }
            if (isFlaky) {
                Flakiness.Flaky(
                    // todo we can't parse annotations default from dex
                    reason = flakyAnnotation.getStringValue(FLAKY_REASON_KEY) ?: NO_REASON
                )
            } else {
                Flakiness.Stable
            }
        }
        else -> Flakiness.Stable
    }
}

private fun determineKind(annotations: List<AnnotationData>): Kind =
    annotations.find { it.name in annotationsToKindMap.keys }
        ?.let { annotationsToKindMap[it.name] }
        ?: Kind.UNKNOWN

private const val DESCRIPTION_VALUE_KEY = "value"

private const val TEST_CASE_ID_VALUE_KEY = "value"

private const val DATA_SET_NUMBER_VALUE_KEY = "value"

private const val EXTERNAL_ID_VALUE_KEY = "value"

private const val TAG_ID_VALUE_KEY = "value"

private const val FEATURE_ID_VALUE_KEY = "value"

private const val FLAKY_REASON_KEY = "reason"

private const val FLAKY_SDKS_KEY = "onSdks"
