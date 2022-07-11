package com.avito.test.summary

public const val testSummaryPluginId: String = "com.avito.android.tests-summary"

public const val testSummaryExtensionName: String = "testSummary"

public fun testSummaryTaskName(appName: String): String = "${appName}TestSummary"

public fun markReportForTmsTaskName(appName: String): String = "${appName}MarkReportForTms"
