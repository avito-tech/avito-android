package com.avito.report.model

import com.avito.android.test.annotations.TestCaseBehavior
import com.avito.android.test.annotations.TestCasePriority

public fun TestStaticDataPackage.Companion.createStubInstance(
    name: String = "com.avito.Test.test",
    deviceName: String = "api22",
    description: String = "just a test",
    testCaseId: Int? = null,
    dataSetNumber: Int? = null,
    externalId: String? = null,
    tagIds: List<Int> = emptyList(),
    featureIds: List<Int> = emptyList(),
    priority: TestCasePriority? = null,
    behavior: TestCaseBehavior? = null,
    kind: Kind = Kind.E2E,
    flakiness: Flakiness = Flakiness.Stable
): TestStaticDataPackage = TestStaticDataPackage(
    name = TestName(name),
    device = DeviceName(deviceName),
    description = description,
    testCaseId = testCaseId,
    dataSetNumber = dataSetNumber,
    externalId = externalId,
    tagIds = tagIds,
    featureIds = featureIds,
    priority = priority,
    behavior = behavior,
    kind = kind,
    flakiness = flakiness
)
