package com.avito.android.test.report.model

import com.avito.android.test.annotations.TestCaseBehavior
import com.avito.android.test.annotations.TestCasePriority
import com.avito.android.test.report.TestPackageParser
import com.avito.report.model.Kind
import java.io.Serializable

data class TestMetadata(
    val caseId: Int?,
    val description: String?,
    val className: String,
    val methodName: String?,
    val dataSetNumber: Int?,
    val testType: TestType,
    val kind: Kind,
    val packageParserResult: TestPackageParser.Result,
    val priority: TestCasePriority?,
    val behavior: TestCaseBehavior?,
    val features: List<String>,
    val externalId: String?,
    val tagIds: List<Int>
) : Serializable
