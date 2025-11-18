package com.avito.android.gradle_configuration.extension

import com.avito.android.model.input.config.CdBuildConfig
import com.avito.android.model.input.config.CdBuildConfigV2
import com.avito.android.model.input.config.CdBuildConfigV3
import com.avito.android.model.input.config.CdBuildConfigV4
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.api.specs.Spec

internal class ShouldUploadToNupokatiSpec(
    val project: Project,
    val cdBuildConfigProvider: Provider<CdBuildConfig>
) : Spec<Task> {
    override fun isSatisfiedBy(t: Task?): Boolean {
        if (!cdBuildConfigProvider.isPresent) {
            project.logger.lifecycle(
                "Skip uploading artifacts and contract json, " +
                    "because cdBuildConfigFile wasn't set"
            )
            return false
        }
        val skipUpload = when (val cdBuildConfig = cdBuildConfigProvider.get()) {
            is CdBuildConfigV2 -> cdBuildConfig.outputDescriptor.skipUpload
            is CdBuildConfigV3 -> cdBuildConfig.outputDescriptor.skipUpload
            is CdBuildConfigV4 -> cdBuildConfig.skipUpload
        }
        if (skipUpload) {
            project.logger.lifecycle(
                "Skip uploading artifacts and contract json, " +
                    "because skipUpload=true is called"
            )
        }
        val shouldRunTask = !skipUpload
        return shouldRunTask
    }
}
