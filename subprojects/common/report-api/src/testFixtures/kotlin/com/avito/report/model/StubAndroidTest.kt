package com.avito.report.model

public fun AndroidTest.Completed.Companion.createStubInstance(
    testStaticData: TestStaticDataPackage = TestStaticDataPackage.createStubInstance(),
    testRuntimeData: TestRuntimeData = TestRuntimeDataPackage.createStubInstance(),
    stdout: String = "",
    stderr: String = ""
): AndroidTest.Completed = create(
    testStaticData = testStaticData,
    testRuntimeData = testRuntimeData,
    stdout = stdout,
    stderr = stderr
)

public fun AndroidTest.Lost.Companion.createStubInstance(
    name: String = "com.avito.Test.test",
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
    stdout: String = "",
    stderr: String = "",
    incident: Incident? = null
): AndroidTest.Lost = fromTestMetadata(
    TestStaticDataPackage(
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
    ),
    startTime = startTime,
    lastSignalTime = lastSignalTime,
    stdout = stdout,
    stderr = stderr,
    incident = incident
)

public fun AndroidTest.Skipped.Companion.createStubInstance(
    name: String = "com.avito.Test.test",
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
    ),
    skipReason = skipReason,
    reportTime = reportTime
)
