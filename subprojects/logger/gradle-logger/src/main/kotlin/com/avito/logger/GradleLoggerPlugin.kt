@file:Suppress("UnstableApiUsage")

package com.avito.logger

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider

public class GradleLoggerPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        checkProjectIsRoot(target)
        val extension = target.extensions.create("gradleLogger", GradleLoggerExtension::class.java)
        extension.fileHandlerRootDir.set(target.layout.buildDirectory.dir("logs"))
        target.afterEvaluate {
            /**
             * The problem is some plugins could start use LoggerService before build script evaluated
             * It leads to state when we initialize LoggerService with empty [GradleLoggerExtension]
             */
            extension.finalizeValues()
        }
    }

    public companion object {

        public fun getLoggerFactory(
            task: Task
        ): Provider<LoggerFactory> {
            val project = task.project
            return getLoggerFactory(project, GradleLoggerCoordinates(project.name, task.name))
        }

        public fun getLoggerFactory(
            project: Project
        ): Provider<LoggerFactory> = getLoggerFactory(project, GradleLoggerCoordinates(project.path))

        private fun getLoggerService(project: Project): Provider<LoggerService> {
            val rootProject = project.rootProject
            require(rootProject.plugins.hasPlugin(GradleLoggerPlugin::class.java)) {
                "com.avito.android.gradle-logger plugin must be added to the root project"
            }
            val extension = rootProject.extensions.getByType(GradleLoggerExtension::class.java)
            return registerLoggerServiceIfAbsent(project, extension)
        }

        private fun getLoggerFactory(
            project: Project,
            coordinates: GradleLoggerCoordinates
        ): Provider<LoggerFactory> {
            return getLoggerService(project)
                .map { it.createLoggerFactory(coordinates) }
        }

        private fun checkProjectIsRoot(target: Project) {
            require(target.rootProject == target) {
                "Must be applied only to root project"
            }
        }

        private fun registerLoggerServiceIfAbsent(
            target: Project,
            extension: GradleLoggerExtension
        ): Provider<LoggerService> {
            return target.gradle.sharedServices.registerIfAbsent(
                LoggerService::javaClass.name,
                LoggerService::class.java
            ) {
                it.parameters {
                    it.appendMetadata.set(extension.appendMetadata)
                    it.printlnHandler.set(extension.printlnHandler)
                    it.fileHandler.set(extension.fileHandler)
                    it.fileHandlerRootDir.set(extension.fileHandlerRootDir)
                    it.elasticHandler.set(extension.elasticHandler)
                    it.sentryHandler.set(extension.sentryHandler)
                    it.finalized.set(extension.finalized)
                }
            }
        }
    }
}
