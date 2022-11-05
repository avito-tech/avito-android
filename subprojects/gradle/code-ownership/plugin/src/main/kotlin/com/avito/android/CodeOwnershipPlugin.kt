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
        if (target.isRoot()) {
            configureDiffReportTask(target)
            configureInfoReportTask(target)
        } else {
            configureStrictOwnershipCheck(target)
        }
    }

    private fun configureStrictOwnershipCheck(target: Project) {
        val codeOwnershipExtension = target.extensions.create<CodeOwnershipExtension>("ownership")
        val strictOwnership = target.getBooleanProperty("avito.ownership.strictOwnership", false)
        if (!strictOwnership) return

        target.afterEvaluate {
            if (it.state.failure == null && codeOwnershipExtension.owners.isEmpty()) {
                throw IllegalStateException(codeOwnershipExtension.emptyOwnersErrorMessage.format(it.path))
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

            outputCsv.set(project.layout.buildDirectory.file("outputs/code-ownership/gradle-modules-owners.csv"))
        }
    }
}
