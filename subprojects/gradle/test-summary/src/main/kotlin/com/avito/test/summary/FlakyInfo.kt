package com.avito.test.summary

import com.avito.test.model.TestName

internal data class FlakyInfo(
    val testName: TestName,
    val attempts: Int,
    val wastedTimeEstimateInSec: Int
)
