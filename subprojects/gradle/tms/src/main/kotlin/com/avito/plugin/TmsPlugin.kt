package com.avito.plugin

import com.avito.android.Problem
import com.avito.android.asPlainText
import com.avito.logger.GradleLoggerFactory
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

// todo stub; remove after 2021.24
class TmsPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.extensions.create<TmsExtension>("tms")

        val logger = GradleLoggerFactory.getLogger(this, target)

        val problem = Problem(
            shortDescription = "TmsPlugin is deprecated, remove it",
            context = "TmsPlugin applied to ${target.path}",
            because = "TmsPlugin functionality now bundled into TestSummaryPlugin"
        )
        logger.warn(problem.asPlainText())
    }
}
