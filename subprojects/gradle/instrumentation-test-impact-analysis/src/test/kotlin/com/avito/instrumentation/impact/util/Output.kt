package com.avito.instrumentation.impact.util

import com.avito.impact.util.RootId
import com.avito.impact.util.Screen
import com.avito.impact.util.Test
import com.avito.instrumentation.impact.BytecodeAnalyzeSummary
import com.avito.instrumentation.impact.ImpactSummary
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import java.io.File

fun impactAnalysisTestsToRunOutput(outputDir: File): Set<String> =
    impactSummary(outputDir).testsToRun

fun impactAnalysisTestsChangedOutput(outputDir: File): Set<Test> =
    impactSummary(outputDir).affectedTests.codeChanges

fun impactAnalysisScreensToTestsOutput(outputDir: File) =
    bytecodeAnalyzeSummary(outputDir).testsByScreen

fun impactAnalysisScreenIdsOutput(outputDir: File): Map<Screen, RootId> =
    bytecodeAnalyzeSummary(outputDir).rootIdByScreen

internal fun impactSummary(outputDir: File): ImpactSummary =
    Gson().fromJson(File(outputDir, "impact-summary.json").readText())

internal fun bytecodeAnalyzeSummary(outputDir: File): BytecodeAnalyzeSummary =
    Gson().fromJson(File(outputDir, "bytecode-analyze-summary.json").readText())
