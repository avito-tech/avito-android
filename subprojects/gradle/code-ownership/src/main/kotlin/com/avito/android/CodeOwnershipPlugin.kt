@file:Suppress("deprecation")

package com.avito.android

import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.isRoot
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

public class CodeOwnershipPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        check(target.isRoot()) { "Code ownership plugin must be applied to the root project" }

        val strictOwnership = target.getBooleanProperty("avito.ownership.strictOwnership", false)

        target.subprojects { subproject ->
            subproject.plugins.withId("kotlin") {
                setupLibrary(subproject, strictOwnership)
            }
            subproject.plugins.withId("com.android.library") {
                setupLibrary(subproject, strictOwnership)
            }
            subproject.plugins.withId("com.android.application") {
                setupLibrary(subproject, strictOwnership)
            }
        }
    }

    private fun setupLibrary(project: Project, strictOwnership: Boolean) {
        val codeOwnershipExtension = project.extensions.create<CodeOwnershipExtension>("ownership")

        project.afterEvaluate {
            if (strictOwnership) {
                codeOwnershipExtension.checkProjectOwnershipSettings(it.path)
            }
        }
    }
}
