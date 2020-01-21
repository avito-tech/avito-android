package com.avito.android

import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.isRoot
import com.avito.utils.gradle.addPreBuildTasks
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

class CodeOwnershipPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        check(target.isRoot()) { "Code ownership plugin must be applied to the root project" }

        val enabled = target.getBooleanProperty("avito.moduleOwnershipValidationEnabled", false)

        target.subprojects { subproject ->
            subproject.plugins.withId("kotlin") {
                setupLibrary(subproject, enabled)
            }
            subproject.plugins.withId("com.android.library") {
                setupLibrary(subproject, enabled)
            }
        }
    }

    private fun setupLibrary(project: Project, enabled: Boolean) {
        val codeOwnershipExtension = project.extensions.create<CodeOwnershipExtension>("ownership")

        val ownershipTask =
            project.tasks.register<CheckProjectDependenciesOwnershipTask>("checkProjectDependenciesOwnership") {
                group = "verification"
                description = "Checks project dependencies validity based on code ownership rules"
                onlyIf { enabled }
            }
        project.addPreBuildTasks(ownershipTask)

        project.afterEvaluate {
            codeOwnershipExtension.checkProjectOwnershipSettings(it.path)
        }
    }
}
