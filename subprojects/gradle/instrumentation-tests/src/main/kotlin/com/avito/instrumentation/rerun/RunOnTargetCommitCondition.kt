package com.avito.instrumentation.rerun

import com.avito.buildontarget.BuildOnTargetCommitForTestTask
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import org.gradle.api.tasks.TaskProvider

object RunOnTargetCommitCondition {

    sealed class Result {
        object No : Result()
        data class Yes(val task: TaskProvider<BuildOnTargetCommitForTestTask>) : Result()
    }

    fun evaluate(
        instrumentationConfiguration: InstrumentationConfiguration.Data,
        hasBuildOnTargetPlugin: Boolean,
        buildOnTargetTaskProvider: () -> TaskProvider<BuildOnTargetCommitForTestTask>
    ): Result {
        val useArtifactsFromTargetBranch = isArtifactsFromTargetBranchNeeded(instrumentationConfiguration)

        return if (useArtifactsFromTargetBranch != null) {
            if (!hasBuildOnTargetPlugin) {
                throw IllegalStateException(
                    "[${instrumentationConfiguration.name}] " +
                        "Plugin com.avito.android.build-on-target is missing, but required, " +
                        "because $useArtifactsFromTargetBranch"
                )
            }
            Result.Yes(buildOnTargetTaskProvider.invoke())
        } else {
            Result.No
        }
    }

    private fun isArtifactsFromTargetBranchNeeded(instrumentationConfiguration: InstrumentationConfiguration.Data): String? {
        return when {
            instrumentationConfiguration.tryToReRunOnTargetBranch -> {
                "tryToReRunOnTargetBranch options enabled in configuration"
            }
            instrumentationConfiguration.performanceType != null -> {
                "it is a performance configuration of type ${instrumentationConfiguration.performanceType}, which needs target commit artifacts"
            }
            else -> null
        }
    }
}
