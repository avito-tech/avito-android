package com.avito.instrumentation

object TestsFilter {

    private val manual = setOf(
        "com.avito.android.test.annotations.ManualTest",
        "com.avito.android.test.annotations.UIComponentStub",
        "com.avito.android.test.annotations.E2EStub"
    )

    val uiNoE2e = setOf(
        "com.avito.android.test.annotations.ComponentTest",
        "com.avito.android.test.annotations.InstrumentationUnitTest",
        "com.avito.android.test.annotations.PublishTest",
        "com.avito.android.test.annotations.MessengerTest",

        "com.avito.android.test.annotations.ScreenshotTest",

        "com.avito.android.test.annotations.UIComponentTest",
        "com.avito.android.test.annotations.IntegrationTest"
    )

    val ui = uiNoE2e + setOf(
        "com.avito.android.test.annotations.FunctionalTest",
        "com.avito.android.test.annotations.E2ETest"
    )

    val regressionNoE2e = uiNoE2e + manual

    val regression = ui + manual

    val performanceNoE2e = setOf("com.avito.android.test.annotations.PerformanceComponentTest")

    val performance = performanceNoE2e + "com.avito.android.test.annotations.PerformanceFunctionalTest"
}