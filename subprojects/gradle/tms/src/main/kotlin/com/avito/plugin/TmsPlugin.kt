package com.avito.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

class TmsPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val extension = target.extensions.create<TmsExtension>("tms")

        target.tasks.register<MarkReportAsSourceTask>(markReportAsSourceTaskName) {
            reportsHost.set(extension.reportsHost)
        }

        target.afterEvaluate {
            requireNotNull(extension.reportsHost.orNull) { "tms.reportsHost should be set" }
        }
    }
}
