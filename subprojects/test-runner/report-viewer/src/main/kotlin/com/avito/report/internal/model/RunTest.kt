package com.avito.report.internal.model

import com.google.gson.annotations.SerializedName

internal data class RunTest(
    @SerializedName("test_name") val testName: String,
    val id: String,
    @SerializedName("run_test_result") val runTestResult: List<RunTestResult>
)
