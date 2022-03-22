@file:Suppress("deprecation")

package com.avito.android

import com.avito.android.diff.ReportCodeOwnershipDiffTask
import com.avito.android.diff.ReportCodeOwnershipExtension
import com.avito.android.info.ReportCodeOwnershipInfoTask
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.isRoot
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

public class CodeOwnershipPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        check(target.isRoot()) { "Code ownership plugin must be applied to the root project" }

        configureStrictOwnershipCheck(target)
        configureDiffReportTask(target)
        configureInfoReportTask(target)
    }

    private fun configureStrictOwnershipCheck(target: Project) {
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

    private fun configureDiffReportTask(target: Project) {
        val extension = target.extensions.create<ReportCodeOwnershipExtension>("codeOwnershipDiffReport")

        target.tasks.register<ReportCodeOwnershipDiffTask>("reportCodeOwnershipDiff") {
            group = "verification"
            description = "Submits report if actual code owners are different from expected ones"

            expectedOwnersProvider.set(extension.expectedOwnersProvider)
            actualOwnersProvider.set(extension.actualOwnersProvider)
            messageFormatter.set(extension.messageFormatter)
            diffReportDestination.set(extension.diffReportDestination)
            comparator.set(extension.comparator)
        }
    }

    private fun configureInfoReportTask(target: Project) {
        target.tasks.register<ReportCodeOwnershipInfoTask>("reportCodeOwnershipInfo") {
            group = "documentation"
            description = "Exports code ownership info for all the modules to CSV file"
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
