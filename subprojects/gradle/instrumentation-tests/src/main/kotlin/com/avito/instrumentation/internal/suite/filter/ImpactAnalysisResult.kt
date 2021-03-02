package com.avito.instrumentation.internal.suite.filter

import java.io.File
import java.io.Serializable

public data class ImpactAnalysisResult(
    val runOnlyChangedTests: Boolean,
    val affectedTests: List<String>,
    val addedTests: List<String>,
    val modifiedTests: List<String>,
    val changedTests: List<String>
) : Serializable {

    public companion object {
        public fun create(
            runOnlyChangedTests: Boolean,
            affectedTestsFile: File?,
            addedTestsFile: File?,
            modifiedTestsFile: File?,
            changedTestsFile: File?
        ): ImpactAnalysisResult = ImpactAnalysisResult(
            runOnlyChangedTests = runOnlyChangedTests,
            affectedTests = parseFile(affectedTestsFile),
            addedTests = parseFile(addedTestsFile),
            modifiedTests = parseFile(modifiedTestsFile),
            changedTests = parseFile(changedTestsFile)
        )

        private fun parseFile(file: File?): List<String> {
            return if (file != null && file.exists()) {
                file.readLines()
            } else {
                emptyList()
            }
        }
    }
}
