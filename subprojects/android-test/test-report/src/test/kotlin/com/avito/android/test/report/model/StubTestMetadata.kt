package com.avito.android.test.report.model

import com.avito.android.test.annotations.TestCaseBehavior
import com.avito.android.test.annotations.TestCasePriority
import com.avito.report.model.Flakiness
import com.avito.report.model.Kind

internal fun TestMetadata.Companion.createStubInstance(
    caseId: Int? = null,
    description: String? = null,
    className: String = "com.Test",
    methodName: String? = "test",
    dataSetNumber: Int? = null,
    kind: Kind = Kind.UNKNOWN,
    priority: TestCasePriority? = null,
    behavior: TestCaseBehavior? = null,
    externalId: String? = null,
    featureIds: List<Int> = emptyList(),
    tagIds: List<Int> = emptyList(),
    flakiness: Flakiness = Flakiness.Stable
) = TestMetadata(
    caseId = caseId,
    description = description,
    className = className,
    methodName = methodName,
    dataSetNumber = dataSetNumber,
    kind = kind,
    priority = priority,
    behavior = behavior,
    externalId = externalId,
    featureIds = featureIds,
    tagIds = tagIds,
    flakiness = flakiness
)
