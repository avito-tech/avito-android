package com.avito.report.model

import com.avito.android.test.annotations.TestCaseBehavior
import com.avito.android.test.annotations.TestCasePriority

fun TestStaticDataPackage.Companion.createStubInstance(
    name: String = "com.avito.Test.test",
    deviceName: String = "api22",
    description: String = "just a test",
    testCaseId: Int? = null,
    dataSetNumber: Int? = null,
    externalId: String? = null,
    features: List<String> = emptyList(),
    tagIds: List<Int> = emptyList(),
    featureIds: List<Int> = emptyList(),
    priority: TestCasePriority? = null,
    behavior: TestCaseBehavior? = null,
    kind: Kind = Kind.E2E
) = TestStaticDataPackage(
    name = TestName(name),
    device = DeviceName(deviceName),
    description = description,
    testCaseId = testCaseId,
    dataSetNumber = dataSetNumber,
    externalId = externalId,
    features = features,
    tagIds = tagIds,
    featureIds = featureIds,
    priority = priority,
    behavior = behavior,
    kind = kind
)

fun TestRuntimeDataPackage.Companion.createStubInstance(
    incident: Incident? = null,
    dataSetData: Map<String, String> = emptyMap(),
    performanceJson: String? = null,
    preconditions: List<Step> = emptyList(),
    startTime: Long = 0,
    endTime: Long = 0,
    steps: List<Step> = emptyList(),
    video: Video? = null
) = TestRuntimeDataPackage(
    incident = incident,
    dataSetData = dataSetData,
    performanceJson = performanceJson,
    preconditions = preconditions,
    startTime = startTime,
    endTime = endTime,
    steps = steps,
    video = video
)

fun AndroidTest.Skipped.Companion.createStubInstance(
    name: String = "com.avito.Test.test",
    deviceName: String = "api22",
    description: String = "just a test",
    reportTime: Long = 0,
    testCaseId: Int? = null,
    dataSetNumber: Int? = null,
    externalId: String? = null,
    features: List<String> = emptyList(),
    tagIds: List<Int> = emptyList(),
    featureIds: List<Int> = emptyList(),
    priority: TestCasePriority? = null,
    behavior: TestCaseBehavior? = null,
    kind: Kind = Kind.E2E,
    skipReason: String = "просто потомучто"
): AndroidTest.Skipped = fromTestMetadata(
    TestStaticDataPackage(
        name = TestName(name),
        device = DeviceName(deviceName),
        description = description,
        testCaseId = testCaseId,
        dataSetNumber = dataSetNumber,
        externalId = externalId,
        features = features,
        tagIds = tagIds,
        featureIds = featureIds,
        priority = priority,
        behavior = behavior,
        kind = kind
    ),
    skipReason = skipReason,
    reportTime = reportTime
)

fun AndroidTest.Lost.Companion.createStubInstance(
    name: String = "com.avito.Test.test",
    deviceName: String = "api22",
    description: String = "just a test",
    startTime: Long = 0,
    lastSignalTime: Long = 0,
    testCaseId: Int? = null,
    dataSetNumber: Int? = null,
    externalId: String? = null,
    features: List<String> = emptyList(),
    tagIds: List<Int> = emptyList(),
    featureIds: List<Int> = emptyList(),
    priority: TestCasePriority? = null,
    behavior: TestCaseBehavior? = null,
    kind: Kind = Kind.E2E,
    stdout: String = "",
    stderr: String = ""
): AndroidTest.Lost = fromTestMetadata(
    TestStaticDataPackage(
        name = TestName(name),
        device = DeviceName(deviceName),
        description = description,
        testCaseId = testCaseId,
        dataSetNumber = dataSetNumber,
        externalId = externalId,
        features = features,
        tagIds = tagIds,
        featureIds = featureIds,
        priority = priority,
        behavior = behavior,
        kind = kind
    ),
    startTime = startTime,
    lastSignalTime = lastSignalTime,
    stdout = stdout,
    stderr = stderr
)

fun AndroidTest.Completed.Companion.createStubInstance(
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
