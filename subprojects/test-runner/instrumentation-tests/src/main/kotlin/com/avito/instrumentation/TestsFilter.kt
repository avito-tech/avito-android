package com.avito.instrumentation

import com.avito.android.test.annotations.E2EStub
import com.avito.android.test.annotations.E2ETest
import com.avito.android.test.annotations.IntegrationTest
import com.avito.android.test.annotations.ManualTest
import com.avito.android.test.annotations.ScreenshotTest
import com.avito.android.test.annotations.UIComponentStub
import com.avito.android.test.annotations.UIComponentTest

private val manualAnnotations = setOf(
    ManualTest::class.java.name,
    UIComponentStub::class.java.name,
    E2EStub::class.java.name
)

private val uiNoE2EAnnotations = setOf(
    ScreenshotTest::class.java.name,
    UIComponentTest::class.java.name,
    IntegrationTest::class.java.name
)

private val uiE2EOnlyAnnotations = setOf(
    E2ETest::class.java.name
)

private val uiAnnotations = uiNoE2EAnnotations + uiE2EOnlyAnnotations

private val regressionNoE2EAnnotations = uiNoE2EAnnotations + manualAnnotations

private val regressionAnnotations = uiAnnotations + manualAnnotations

public enum class TestsFilter(public val annotatedWith: Set<String>) {
    uiE2EOnly(uiE2EOnlyAnnotations),
    uiNoE2E(uiNoE2EAnnotations),
    ui(uiAnnotations),
    regressionNoE2E(regressionNoE2EAnnotations),
    regression(regressionAnnotations),
    empty(emptySet()),
    manual(manualAnnotations)
}
