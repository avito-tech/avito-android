package com.avito.runner.scheduler.suite.filter

import java.io.File
import java.io.Serializable

public data class ImpactAnalysisResult(
    val selection: ImpactAnalysisSelection,
    val changedTests: List<String>
) : Serializable {

    @Deprecated("Use selection; ByTarget has no global mode")
    public val mode: ImpactAnalysisMode
        get() = (selection as? ImpactAnalysisSelection.Uniform)?.mode
            ?: error("ByTarget has no global impact analysis mode")

    public constructor(mode: ImpactAnalysisMode, changedTests: List<String>) :
        this(ImpactAnalysisSelection.Uniform(mode), changedTests)

    public companion object {

        public fun create(
            selection: ImpactAnalysisSelection,
            changedTestsFile: File?
        ): ImpactAnalysisResult = ImpactAnalysisResult(
            selection = selection,
            changedTests = parseFile(changedTestsFile)
        )

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
