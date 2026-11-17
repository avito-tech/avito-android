package com.avito.runner.scheduler.suite.filter

import java.io.Serializable

/** Defines changed-test filtering modes for the targets of a run. */
public sealed interface TargetFilterMode : Serializable {

    /** Applies the same mode to every target. */
    public data class Uniform(val mode: ChangedTestsFilterMode) : TargetFilterMode

    /** Each target must have an explicit mode; there is no fallback to a global mode. */
    public data class ByTarget(val modesByTarget: Map<String, ChangedTestsFilterMode>) : TargetFilterMode {

        init {
            require(modesByTarget.isNotEmpty()) { "ByTarget requires at least one target" }
            require(modesByTarget.keys.none { it.isBlank() }) { "Target names must not be blank" }
        }

        public fun validateTargets(targets: Set<String>) {
            require(modesByTarget.keys == targets) {
                "Target filter mode mismatch: missing=${targets - modesByTarget.keys}, " +
                    "unknown=${modesByTarget.keys - targets}"
            }
        }
    }
}
