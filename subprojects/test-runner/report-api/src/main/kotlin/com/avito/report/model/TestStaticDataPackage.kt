package com.avito.report.model

import com.avito.android.test.annotations.TestCaseBehavior
import com.avito.android.test.annotations.TestCasePriority
import com.avito.test.model.DeviceName
import com.avito.test.model.TestName

public data class TestStaticDataPackage(
    override val name: TestName,
    override val device: DeviceName,
    override val description: String?,
    override val testCaseId: Int?,
    override val dataSetNumber: Int?,
    override val externalId: String?,
    override val featureIds: List<Int>,
    override val tagIds: List<Int>,
    override val priority: TestCasePriority?,
    override val behavior: TestCaseBehavior?,
    override val kind: Kind,
    override val flakiness: Flakiness
) : TestStaticData {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TestStaticData) return false

        if (name != other.name) return false
        if (device != other.device) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + device.hashCode()
        return result
    }

    // for test fixtures
    public companion object
}
