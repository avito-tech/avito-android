package com.avito.instrumentation

private val manualAnnotations = setOf(
    "com.avito.android.test.annotations.ManualTest",
    "com.avito.android.test.annotations.UIComponentStub",
    "com.avito.android.test.annotations.E2EStub"
)

private val uiNoE2EAnnotations = setOf(
    "com.avito.android.test.annotations.ComponentTest",
    "com.avito.android.test.annotations.InstrumentationUnitTest",
    "com.avito.android.test.annotations.PublishTest",
    "com.avito.android.test.annotations.MessengerTest",

    "com.avito.android.test.annotations.ScreenshotTest",

    "com.avito.android.test.annotations.UIComponentTest",
    "com.avito.android.test.annotations.IntegrationTest"
)

private val uiAnnotations = uiNoE2EAnnotations + setOf(
    "com.avito.android.test.annotations.FunctionalTest",
    "com.avito.android.test.annotations.E2ETest"
)

private val regressionNoE2EAnnotations = uiNoE2EAnnotations + manualAnnotations

private val regressionAnnotations = uiAnnotations + manualAnnotations

private val performanceNoE2EAnnotations =
    setOf("com.avito.android.test.annotations.PerformanceComponentTest")

private val performanceAnnotations =
    performanceNoE2EAnnotations + "com.avito.android.test.annotations.PerformanceFunctionalTest"

enum class TestsFilter(val annotatedWith: Set<String>) {
    uiNoE2E(uiNoE2EAnnotations),
    ui(uiAnnotations),
    regressionNoE2E(regressionNoE2EAnnotations),
    regression(regressionAnnotations),
    performanceNoE2E(performanceNoE2EAnnotations),
    performance(performanceAnnotations),
    empty(emptySet()),
    manual(manualAnnotations)
}
