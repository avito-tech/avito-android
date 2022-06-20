package com.avito.ci.steps.deploy

import com.avito.capitalize
import com.avito.cd.model.BuildVariant
import org.gradle.api.Project

internal class UploadCrashlyticsMappingFileTasksProvider : CrashlyticsTaskProvider() {

    override fun taskProvider(
        project: Project,
        buildVariant: BuildVariant
    ): String {
        val projectName = project.name
        val buildVariantCapitalized = buildVariant.name
            .lowercase()
            .capitalize()
        /**
         * TODO when I try to find task provider via project.tasks.named I get Exception that there is no task
         * That's because firebase-crashlytics-plugin creates task some how after we trying bind to it
         */
        return ":$projectName:uploadCrashlyticsMappingFile$buildVariantCapitalized"
    }
}
