package com.avito.runner.scheduler.suite.filter

import java.io.File
import java.io.Serializable

public data class ImpactAnalysisResult(
    val runOnlyChangedTests: Boolean,
    val changedTests: List<String>
) : Serializable {

    public companion object {

        public fun create(
            runOnlyChangedTests: Boolean,
            changedTestsFile: File?
        ): ImpactAnalysisResult = ImpactAnalysisResult(
            runOnlyChangedTests = runOnlyChangedTests,
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
