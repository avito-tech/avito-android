package com.avito.instrumentation.suite.filter

import com.avito.instrumentation.configuration.ImpactAnalysisPolicy
import java.io.File

data class ImpactAnalysisResult(
    val policy: ImpactAnalysisPolicy,
    val affectedTests: List<String>,
    val addedTests: List<String>,
    val modifiedTests: List<String>
) {

    companion object {
        fun create(
            policy: ImpactAnalysisPolicy,
            affectedTestsFile: File?,
            addedTestsFile: File?,
            modifiedTestsFile: File?
        ) = ImpactAnalysisResult(
            policy = policy,
            affectedTests = parseFile(affectedTestsFile),
            addedTests = parseFile(addedTestsFile),
            modifiedTests = parseFile(modifiedTestsFile)
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
