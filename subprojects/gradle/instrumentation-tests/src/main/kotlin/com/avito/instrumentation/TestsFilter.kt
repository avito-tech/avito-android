package com.avito.instrumentation

import com.avito.android.test.annotations.E2EStub
import com.avito.android.test.annotations.E2ETest
import com.avito.android.test.annotations.IntegrationTest
import com.avito.android.test.annotations.ManualTest
import com.avito.android.test.annotations.ScreenshotTest
import com.avito.android.test.annotations.UIComponentStub
import com.avito.android.test.annotations.UIComponentTest

private val manualAnnotations = setOf(
    ManualTest::javaClass.name,
    UIComponentStub::javaClass.name,
    E2EStub::javaClass.name
)

private val uiNoE2EAnnotations = setOf(
    ScreenshotTest::javaClass.name,
    UIComponentTest::javaClass.name,
    IntegrationTest::javaClass.name
)

private val uiE2EOnlyAnnotations = setOf(
    E2ETest::javaClass.name
)

private val uiAnnotations = uiNoE2EAnnotations + uiE2EOnlyAnnotations

private val regressionNoE2EAnnotations = uiNoE2EAnnotations + manualAnnotations

private val regressionAnnotations = uiAnnotations + manualAnnotations

enum class TestsFilter(val annotatedWith: Set<String>) {
    uiE2EOnly(uiE2EOnlyAnnotations),
    uiNoE2E(uiNoE2EAnnotations),
    ui(uiAnnotations),
    regressionNoE2E(regressionNoE2EAnnotations),
    regression(regressionAnnotations),
    empty(emptySet()),
    manual(manualAnnotations)
}
