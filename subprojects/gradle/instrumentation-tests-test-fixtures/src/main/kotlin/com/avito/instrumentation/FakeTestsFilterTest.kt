package com.avito.instrumentation

import com.avito.instrumentation.suite.dex.AnnotationData
import com.avito.instrumentation.suite.filter.TestsFilter
import com.avito.report.model.DeviceName
import com.avito.report.model.Flakiness

fun TestsFilter.Test.Companion.createStub(
    name: String = "Stub",
    annotations: List<AnnotationData> = emptyList(),
    deviceName: DeviceName = DeviceName("stub"),
    api: Int = 21,
    flakiness: Flakiness = Flakiness.Stable
): TestsFilter.Test =
    TestsFilter.Test(
        name = name,
        annotations = annotations,
        deviceName = deviceName,
        api = api,
        flakiness = flakiness
    )