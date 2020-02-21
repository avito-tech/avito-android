package com.avito.instrumentation.rerun

import com.avito.instrumentation.configuration.InstrumentationConfiguration

object RunOnTargetBranchCondition {

    sealed class Result {
        object No : Result()
        data class Yes(val reason: String) : Result()
    }

    fun evaluate(instrumentationConfiguration: InstrumentationConfiguration.Data): Result = when {

        instrumentationConfiguration.tryToReRunOnTargetBranch ->
            Result.Yes("tryToReRunOnTargetBranch options enabled in configuration")

        instrumentationConfiguration.performanceType != null ->
            Result.Yes("is a performance configuration of type ${instrumentationConfiguration.performanceType}")

        else -> Result.No
    }
}
