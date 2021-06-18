package com.avito.report.model

public fun TestStaticDataPackage.Companion.fromSimpleRunTest(simpleRunTest: SimpleRunTest): TestStaticDataPackage =
    TestStaticDataPackage(
        name = simpleRunTest.name,
        device = DeviceName(simpleRunTest.deviceName),
        description = simpleRunTest.description,
        testCaseId = simpleRunTest.testCaseId,
        dataSetNumber = simpleRunTest.dataSetNumber,
        externalId = simpleRunTest.externalId,
        featureIds = simpleRunTest.featureIds,
        tagIds = simpleRunTest.tagIds,
        priority = simpleRunTest.priority,
        behavior = simpleRunTest.behavior,
        kind = simpleRunTest.kind,
        flakiness = simpleRunTest.flakiness
    )
