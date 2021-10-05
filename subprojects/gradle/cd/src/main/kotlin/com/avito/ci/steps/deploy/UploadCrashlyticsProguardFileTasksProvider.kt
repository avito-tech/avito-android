package com.avito.ci.steps.deploy

import com.avito.capitalize
import com.avito.cd.BuildVariant
import com.avito.cd.CdBuildConfig
import org.gradle.api.Project

internal class UploadCrashlyticsProguardFileTasksProvider {

    fun provide(
        project: Project,
        deployments: List<CdBuildConfig.Deployment.GooglePlay>
    ): List<String> = deployments
        .groupBy { it.buildVariant }
        .keys
        .map { buildVariant ->
            uploadCrashlyticsProguardFileTaskProvider(project, buildVariant)
        }

    private fun uploadCrashlyticsProguardFileTaskProvider(
        project: Project,
        buildVariant: BuildVariant
    ): String {
        val projectName = project.name
        val buildVariantCapitalized =
            buildVariant.toString()
                .lowercase()
                .capitalize()
        /**
         * TODO when I try to find task provider via project.tasks.named I get Exception that there is no task
         * That's because firebase-crashlytics-plugin creates task some how after we trying bind to it
         */
        return ":$projectName:uploadCrashlyticsMappingFile$buildVariantCapitalized"
    }
}
