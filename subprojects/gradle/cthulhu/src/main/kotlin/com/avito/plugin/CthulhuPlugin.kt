package com.avito.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

class CthulhuPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val extension = target.extensions.create<CthulhuExtension>("cthulhu")

        target.tasks.register<MarkReportAsSourceTask>(markReportAsSourceTaskName) {
            reportsHost.set(extension.reportsHost)
        }
    }
}
