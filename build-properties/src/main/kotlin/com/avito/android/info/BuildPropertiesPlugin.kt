package com.avito.android.info

import com.avito.utils.gradle.addPreBuildTasksIfApplication
import com.avito.utils.logging.CILogger
import com.avito.utils.logging.ciLogger
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.WriteProperties
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

open class BuildPropertiesPlugin : Plugin<Project> {

    private lateinit var logger: CILogger

    override fun apply(project: Project) {
        logger = project.ciLogger

        val config = project.extensions.create<BuildInfoExtension>("buildInfo")

        val generateTaskProvider = project.tasks.register<WriteProperties>("generateAppBuildProperties") {
            property("GIT_COMMIT", config.gitCommit.orEmpty())
            property("GIT_BRANCH", config.gitBranch.orEmpty())
            property("BUILD_NUMBER", config.buildNumber.orEmpty())
            outputFile = project.file("src/main/assets/app-build-info.properties")
        }
        project.addPreBuildTasksIfApplication(generateTaskProvider)
    }

}
