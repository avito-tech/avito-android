package com.avito.instrumentation.rerun

import com.avito.buildontarget.BuildOnTargetCommitForTestTask
import com.avito.buildontarget.buildOnTargetTask
import com.avito.buildontarget.hasBuildOnTargetPlugin
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

class RunOnTargetCommitCondition(private val project: Project) {

    sealed class Result {
        object No : Result()
        data class Yes(val task: TaskProvider<BuildOnTargetCommitForTestTask>) : Result()
    }

    fun evaluate(instrumentationConfiguration: InstrumentationConfiguration.Data): Result {
        val useArtifactsFromTargetBranch = isArtifactsFromTargetBranchNeeded(instrumentationConfiguration)

        return if (useArtifactsFromTargetBranch != null) {
            if (!project.pluginManager.hasBuildOnTargetPlugin()) {
                throw IllegalStateException(
                    "[${instrumentationConfiguration.name}] " +
                        "Plugin com.avito.android.build-on-target is missing, but required, " +
                        "because $useArtifactsFromTargetBranch"
                )
            }
            Result.Yes(project.tasks.buildOnTargetTask())
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
