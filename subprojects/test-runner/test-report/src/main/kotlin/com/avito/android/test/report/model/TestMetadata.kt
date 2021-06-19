package com.avito.android.test.report.model

import com.avito.android.test.annotations.TestCaseBehavior
import com.avito.android.test.annotations.TestCasePriority
import com.avito.report.model.Flakiness
import com.avito.report.model.Kind
import com.avito.report.model.TestName
import java.io.Serializable

data class TestMetadata(
    val caseId: Int?,
    val description: String?,
    val name: TestName,
    val dataSetNumber: Int?,
    val kind: Kind,
    val priority: TestCasePriority?,
    val behavior: TestCaseBehavior?,
    val externalId: String?,
    val featureIds: List<Int>,
    val tagIds: List<Int>,
    val flakiness: Flakiness
) : Serializable {

    companion object
}
