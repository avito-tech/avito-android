package com.avito.report.model

import com.avito.android.test.annotations.TestCaseBehavior
import com.avito.android.test.annotations.TestCasePriority

public interface AvitoSpecificTestStaticData {

    public val testCaseId: Int?

    public val externalId: String?

    public val featureIds: List<Int>

    public val tagIds: List<Int>

    public val priority: TestCasePriority?

    public val behavior: TestCaseBehavior?

    public val kind: Kind
}
