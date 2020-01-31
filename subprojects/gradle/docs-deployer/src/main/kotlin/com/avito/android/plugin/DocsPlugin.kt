package com.avito.android.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import java.io.File

@Suppress("UnstableApiUsage")
class DocsPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val docs = File(target.rootDir, "docs")

        val extension = target.extensions.create<DocsExtension>("docsDeploy")

        target.tasks.register<DocsCheckTask>(docsCheckTaskName) {
            group = JavaBasePlugin.DOCUMENTATION_GROUP
            docsDirectory.set(docs)

        }

        target.tasks.register<DeployDocsTask>(docsDeployTaskName) {
            group = JavaBasePlugin.DOCUMENTATION_GROUP
            docsDirectory.set(docs)
            registry.set(extension.registry)
            targetHost.set(extension.targetHost)
        }
    }
}
