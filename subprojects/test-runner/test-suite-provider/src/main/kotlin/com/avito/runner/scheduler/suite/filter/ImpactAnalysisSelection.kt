package com.avito.runner.scheduler.suite.filter

import java.io.Serializable

/** Selects one impact analysis policy for a run. */
public sealed interface ImpactAnalysisSelection : Serializable {

    /** Applies the same mode to every target. */
    public data class Uniform(val mode: ImpactAnalysisMode) : ImpactAnalysisSelection

    /** Each target must have an explicit mode; there is no fallback to a global mode. */
    public data class ByTarget(val modes: Map<String, ImpactAnalysisMode>) : ImpactAnalysisSelection {

        init {
            require(modes.isNotEmpty()) { "ByTarget requires at least one target" }
            require(modes.keys.none { it.isBlank() }) { "Target names must not be blank" }
        }

        public fun validateTargets(targets: Set<String>) {
            require(modes.keys == targets) {
                "Impact analysis target mismatch: missing=${targets - modes.keys}, unknown=${modes.keys - targets}"
            }
        }
    }
}
