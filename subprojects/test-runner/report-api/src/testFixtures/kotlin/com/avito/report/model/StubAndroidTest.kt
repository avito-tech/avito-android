package com.avito.report.model

import com.avito.android.test.annotations.TestCaseBehavior
import com.avito.android.test.annotations.TestCasePriority
import com.avito.test.model.DeviceName
import com.avito.test.model.TestName

public fun AndroidTest.Completed.Companion.createStubInstance(
    testStaticData: TestStaticData = TestStaticDataPackage.createStubInstance(),
    testRuntimeData: TestRuntimeData = TestRuntimeDataPackage.createStubInstance(),
    logcat: String = ""
): AndroidTest.Completed = create(
    testStaticData = testStaticData,
    testRuntimeData = testRuntimeData,
    logcat = logcat
)

public fun AndroidTest.Lost.Companion.createStubInstance(
    testStaticData: TestStaticData = TestStaticDataPackage.createStubInstance(),
    startTime: Long = 0,
    lastSignalTime: Long = 0,
    stdout: String = "",
    incident: Incident? = null
): AndroidTest.Lost = fromTestStaticData(
    testStaticData = testStaticData,
    startTime = startTime,
    lastSignalTime = lastSignalTime,
    logcat = stdout,
    incident = incident
)

public fun AndroidTest.Lost.Companion.createStubInstance(
    className: String = "com.avito.Test",
    methodName: String = "test",
    deviceName: String = "api22",
    description: String = "just a test",
    startTime: Long = 0,
    lastSignalTime: Long = 0,
    testCaseId: Int? = null,
    dataSetNumber: Int? = null,
    externalId: String? = null,
    tagIds: List<Int> = emptyList(),
    featureIds: List<Int> = emptyList(),
    priority: TestCasePriority? = null,
    behavior: TestCaseBehavior? = null,
    kind: Kind = Kind.E2E,
    flakiness: Flakiness = Flakiness.Stable,
    logcat: String = "",
    incident: Incident? = null
): AndroidTest.Lost = fromTestStaticData(
    TestStaticDataPackage(
        name = TestName(className, methodName),
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
    ),
    startTime = startTime,
    lastSignalTime = lastSignalTime,
    logcat = logcat,
    incident = incident
)

public fun AndroidTest.Skipped.Companion.createStubInstance(
    className: String = "com.avito.Test",
    methodName: String = "test",
    deviceName: String = "api22",
    description: String = "just a test",
    reportTime: Long = 0,
    testCaseId: Int? = null,
    dataSetNumber: Int? = null,
    externalId: String? = null,
    tagIds: List<Int> = emptyList(),
    featureIds: List<Int> = emptyList(),
    priority: TestCasePriority? = null,
    behavior: TestCaseBehavior? = null,
    kind: Kind = Kind.E2E,
    flakiness: Flakiness = Flakiness.Stable,
    skipReason: String = "просто потомучто"
): AndroidTest.Skipped = fromTestMetadata(
    TestStaticDataPackage(
        name = TestName(className, methodName),
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
    ),
    skipReason = skipReason,
    reportTime = reportTime
)
