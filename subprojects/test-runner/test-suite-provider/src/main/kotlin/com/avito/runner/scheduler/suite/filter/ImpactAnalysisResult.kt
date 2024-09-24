package com.avito.runner.scheduler.suite.filter

import java.io.File
import java.io.Serializable

public data class ImpactAnalysisResult(
    val mode: ImpactAnalysisMode,
    val changedTests: List<String>
) : Serializable {

    public companion object {

        public fun create(
            mode: ImpactAnalysisMode,
            changedTestsFile: File?
        ): ImpactAnalysisResult = ImpactAnalysisResult(
            mode = mode,
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
