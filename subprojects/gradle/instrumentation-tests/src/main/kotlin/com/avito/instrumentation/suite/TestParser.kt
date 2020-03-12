package com.avito.instrumentation.suite

import com.avito.android.test.annotations.Behavior
import com.avito.android.test.annotations.Priority
import com.avito.android.test.annotations.TestCaseBehavior
import com.avito.android.test.annotations.TestCasePriority
import com.avito.instrumentation.suite.dex.AnnotationData
import com.avito.instrumentation.suite.dex.TestInApk
import com.avito.report.model.DeviceName
import com.avito.report.model.Flakiness
import com.avito.report.model.Kind
import com.avito.report.model.TestStaticData
import com.avito.report.model.TestStaticDataPackage

internal fun parseTest(testInApk: TestInApk, deviceName: DeviceName): TestStaticData = TestStaticDataPackage(
    name = testInApk.testName,

    device = deviceName,

    description = testInApk.annotations
        .find { it.name == DESCRIPTION_NAME }
        ?.getStringValue(DESCRIPTION_VALUE_KEY),

    testCaseId = testInApk.annotations
        .find { it.name == TEST_CASE_ID_NAME }
        ?.getIntValue(TEST_CASE_ID_VALUE_KEY),

    dataSetNumber = testInApk.annotations
        .find { it.name == DATA_SET_NUMBER_NAME }
        ?.getIntValue(DATA_SET_NUMBER_VALUE_KEY),

    externalId = testInApk.annotations
        .find { it.name == EXTERNAL_ID_NAME }
        ?.getStringValue(EXTERNAL_ID_VALUE_KEY),

    tagIds = testInApk.annotations
        .find { it.name == TAG_ID_NAME }
        ?.getIntArrayValue(TAG_ID_VALUE_KEY) ?: emptyList(),

    featureIds = testInApk.annotations
        .find { it.name == FEATURE_ID_NAME }
        ?.getIntArrayValue(FEATURE_ID_VALUE_KEY) ?: emptyList(),

    priority = testInApk.annotations
        .find { it.name == Priority::class.java.name }
        ?.getEnumValue("priority")?.let { TestCasePriority.fromName(it) },

    behavior = testInApk.annotations
        .find { it.name == Behavior::class.java.name }
        ?.getEnumValue("behavior")?.let { TestCaseBehavior.fromName(it) },

    kind = determineKind(testInApk.annotations),

    flakiness = testInApk.annotations
        .find { it.name == FLAKY_NAME }
        ?.getStringValue(FLAKY_REASON_KEY)
        ?.let { reason -> Flakiness.Flaky(reason)}
        ?: Flakiness.Stable
)

private val annotationsToKindMap = mapOf(
    "com.avito.android.test.annotations.FunctionalTest" to Kind.E2E,
    "com.avito.android.test.annotations.ComponentTest" to Kind.UI_COMPONENT,
    "com.avito.android.test.annotations.PublishTest" to Kind.UI_COMPONENT,
    "com.avito.android.test.annotations.MessengerTest" to Kind.UI_COMPONENT,
    "com.avito.android.test.annotations.InfrastructureTest" to Kind.INTEGRATION,
    "com.avito.android.test.annotations.InstrumentationUnitTest" to Kind.INTEGRATION,
    "com.avito.android.test.annotations.ManualTest" to Kind.MANUAL,
    "com.avito.android.test.annotations.PerformanceFunctionalTest" to Kind.E2E,
    "com.avito.android.test.annotations.PerformanceComponentTest" to Kind.UI_COMPONENT,
    "com.avito.android.test.annotations.ScreenshotTest" to Kind.UI_COMPONENT
)

private fun determineKind(annotations: List<AnnotationData>): Kind =
    annotations.find { it.name in annotationsToKindMap.keys }
        ?.let { annotationsToKindMap[it.name] }
        ?: Kind.UNKNOWN

// todo use real classes

private const val DESCRIPTION_NAME = "com.avito.android.test.annotations.Description"
private const val DESCRIPTION_VALUE_KEY = "value"

private const val TEST_CASE_ID_NAME = "com.avito.android.test.annotations.CaseId"
private const val TEST_CASE_ID_VALUE_KEY = "value"

private const val DATA_SET_NUMBER_NAME = "com.avito.android.test.annotations.DataSetNumber"
private const val DATA_SET_NUMBER_VALUE_KEY = "value"

private const val EXTERNAL_ID_NAME = "com.avito.android.test.annotations.ExternalId"
private const val EXTERNAL_ID_VALUE_KEY = "value"

private const val FEATURES_NAME = "com.avito.android.test.annotations.Feature"
private const val FEATURES_VALUE_KEY = "value"

private const val TAG_ID_NAME = "com.avito.android.test.annotations.TagId"
private const val TAG_ID_VALUE_KEY = "value"

private const val FEATURE_ID_NAME = "com.avito.android.test.annotations.FeatureId"
private const val FEATURE_ID_VALUE_KEY = "value"

private const val FLAKY_NAME = "com.avito.android.test.annotations.Flaky"
private const val FLAKY_REASON_KEY = "reason"
