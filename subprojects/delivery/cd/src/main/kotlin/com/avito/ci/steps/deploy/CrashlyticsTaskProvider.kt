package com.avito.ci.steps.deploy

import com.avito.cd.BuildVariant
import com.avito.cd.CdBuildConfig
import org.gradle.api.Project

internal abstract class CrashlyticsTaskProvider {

    fun provide(
        project: Project,
        deployments: List<CdBuildConfig.Deployment.GooglePlay>
    ): List<String> = deployments
        .groupBy { it.buildVariant }
        .keys
        .map { buildVariant ->
            taskProvider(project, buildVariant)
        }

    protected abstract fun taskProvider(
        project: Project,
        buildVariant: BuildVariant
    ): String
}
