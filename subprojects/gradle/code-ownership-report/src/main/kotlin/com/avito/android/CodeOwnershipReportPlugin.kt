package com.avito.android

import com.avito.android.diff.ReportCodeOwnershipDiffTask
import com.avito.android.diff.ReportCodeOwnershipExtension
import com.avito.android.info.ReportCodeOwnershipInfoTask
import com.avito.kotlin.dsl.isRoot
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

public class CodeOwnershipReportPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        check(target.isRoot()) { "Code ownership diff report plugin must be applied to the root project" }

        val extension = target.extensions.create<ReportCodeOwnershipExtension>("codeOwnershipDiffReport")

        target.tasks.register<ReportCodeOwnershipDiffTask>("reportCodeOwnershipDiff") {
            group = "verification"
            description = "Submits report if actual code owners are different from expected ones"

            expectedOwnersExtractor.set(extension.expectedOwnersExtractor)
            actualOwnersExtractor.set(extension.actualOwnersExtractor)
            messageFormatter.set(extension.messageFormatter)
            diffReportDestination.set(extension.diffReportDestination)
            comparator.set(extension.comparator)
        }

        target.tasks.register<ReportCodeOwnershipInfoTask>("reportCodeOwnershipInfo") {
            group = "documentation"
            description = "Exports code ownership info for all the modules to CSV file"
        }
    }
}
