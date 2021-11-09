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
        extension.appendMetadata.set(true)
        target.afterEvaluate {
            /**
             * The problem is some plugins could start use LoggerService before build script evaluated
             * It leads to state when we initialize LoggerService with empty [GradleLoggerExtension]
             */
            extension.finalizeValues()
        }
    }

    public companion object {

        internal const val error = "com.avito.android.gradle-logger plugin must be added to the root project"

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
            return if (rootProject.plugins.hasPlugin(GradleLoggerPlugin::class.java)) {
                val extension = rootProject.extensions.getByType(GradleLoggerExtension::class.java)
                registerLoggerServiceIfAbsent(project, extension)
            } else {
                project.logger.warn(error)
                @Suppress("DEPRECATION")
                legacyRegisterLoggerServiceIfAbsent(rootProject)
            }
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
                error
            }
        }

        @Deprecated("remove after 2021.38 release", replaceWith = ReplaceWith("registerLoggerServiceIfAbsent"))
        private fun legacyRegisterLoggerServiceIfAbsent(
            target: Project,
        ): Provider<LoggerService> {
            return target.gradle.sharedServices.registerIfAbsent(
                LoggerService::javaClass.name,
                LoggerService::class.java
            ) {
                @Suppress("DEPRECATION")
                LegacyGradleLoggerConfigurator(target).configure(it.parameters)
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
