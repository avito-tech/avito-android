package com.avito.reportviewer.model

import com.avito.android.test.annotations.TestCaseBehavior
import com.avito.android.test.annotations.TestCasePriority
import com.avito.report.model.Flakiness
import com.avito.report.model.Kind
import com.avito.report.model.Stability
import com.avito.report.model.TestStatus
import com.avito.test.model.TestName

public fun SimpleRunTest.Companion.createStubInstance(
    id: String = "1234",
    reportId: String = "12345",
    name: TestName = TestName("com.Test", "test"),
    testCaseId: Int? = null,
    deviceName: String = "api22",
    tcBuild: String = "12345",
    groupList: List<String> = emptyList(),
    status: TestStatus = TestStatus.Success,
    stability: Stability = Stability.Stable(1, 1),
    skipReason: String? = null,
    isFinished: Boolean = false,
    lastAttemptDurationInSeconds: Int = 123,
    externalId: String? = null,
    description: String? = null,
    startTime: Long = 0,
    endTime: Long = 0,
    dataSetNumber: Int? = null,
    features: List<String> = emptyList(),
    tagIds: List<Int> = emptyList(),
    featureIds: List<Int> = emptyList(),
    priority: TestCasePriority? = null,
    behavior: TestCaseBehavior? = null,
    kind: Kind = Kind.E2E,
    flakiness: Flakiness = Flakiness.Stable
): SimpleRunTest = SimpleRunTest(
    id = id,
    reportId = reportId,
    name = name,
    testCaseId = testCaseId,
    deviceName = deviceName,
    groupList = groupList,
    status = status,
    stability = stability,
    skipReason = skipReason,
    buildId = tcBuild,
    isFinished = isFinished,
    lastAttemptDurationInSeconds = lastAttemptDurationInSeconds,
    externalId = externalId,
    description = description,
    startTime = startTime,
    endTime = endTime,
    dataSetNumber = dataSetNumber,
    features = features,
    tagIds = tagIds,
    featureIds = featureIds,
    priority = priority,
    behavior = behavior,
    kind = kind,
    flakiness = flakiness
)
