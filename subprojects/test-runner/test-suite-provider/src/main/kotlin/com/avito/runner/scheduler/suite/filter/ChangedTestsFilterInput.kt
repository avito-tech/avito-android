package com.avito.runner.scheduler.suite.filter

import java.io.File
import java.io.Serializable

public data class ChangedTestsFilterInput(
    val targetFilterMode: TargetFilterMode,
    val changedTests: List<String>
) : Serializable {

    /** Compatibility view for the legacy runner, which only uses Uniform. */
    public val mode: ChangedTestsFilterMode
        get() = (targetFilterMode as? TargetFilterMode.Uniform)?.mode
            ?: error("ByTarget has no global changed tests filter mode")

    public constructor(mode: ChangedTestsFilterMode, changedTests: List<String>) :
        this(TargetFilterMode.Uniform(mode), changedTests)

    public companion object {

        public fun create(
            targetFilterMode: TargetFilterMode,
            changedTestsFile: File?
        ): ChangedTestsFilterInput = ChangedTestsFilterInput(
            targetFilterMode = targetFilterMode,
            changedTests = parseFile(changedTestsFile)
        )

        public fun create(
            mode: ChangedTestsFilterMode,
            changedTestsFile: File?
        ): ChangedTestsFilterInput = ChangedTestsFilterInput(
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
