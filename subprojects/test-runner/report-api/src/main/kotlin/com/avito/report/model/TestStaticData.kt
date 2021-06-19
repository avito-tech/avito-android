package com.avito.report.model

import com.avito.android.test.annotations.TestCaseBehavior
import com.avito.android.test.annotations.TestCasePriority

/**
 * Test data that can be parsed even without actual test run
 */
public interface TestStaticData {

    public val name: TestName

    public val device: DeviceName

    public val description: String?

    public val dataSetNumber: Int?

    public val flakiness: Flakiness

    public val testCaseId: Int?

    public val externalId: String?

    public val featureIds: List<Int>

    public val tagIds: List<Int>

    public val priority: TestCasePriority?

    public val behavior: TestCaseBehavior?

    public val kind: Kind
}
