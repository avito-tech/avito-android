package com.avito.instrumentation

import com.avito.instrumentation.suite.dex.AnnotationData
import com.avito.instrumentation.suite.filter.TestsFilter
import com.avito.report.model.DeviceName

fun TestsFilter.Test.Companion.createStub(
    name: String = "Stub",
    annotations: List<AnnotationData> = emptyList(),
    deviceName: DeviceName = DeviceName("stub"),
    api: Int = 21
): TestsFilter.Test =
    TestsFilter.Test(
        name = name,
        annotations = annotations,
        deviceName = deviceName,
        api = api
    )