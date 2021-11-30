@file:Suppress("UnstableApiUsage")

package com.avito.logger

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

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

        /**
         * Inlined to skip anonymous class creation for map lambda
         * Breaks configuration cache with:
         *  > Could not load the value of field `transformer` of
         *  `org.gradle.api.internal.provider.TransformBackedProvider`
         *  > bean found in field `provider` of `org.gradle.api.internal.provider.MappingProvider`
         *  bean found in field `__loggerFactory__`
         */
        @Suppress("NOTHING_TO_INLINE")
        private inline fun getLoggerFactory(
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
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")
                    .withZone(ZoneId.from(ZoneOffset.UTC))
                val instant = Instant.now()
                it.parameters {
                    it.printlnHandler.set(extension.printlnHandler)
                    it.fileHandler.set(extension.fileHandler)
                    it.fileHandlerRootDir.set(extension.fileHandlerRootDir.map { it.dir(formatter.format(instant)) })
                    it.elasticHandler.set(extension.elasticHandler)
                    it.sentryHandler.set(extension.sentryHandler)
                    it.finalized.set(extension.finalized)
                }
            }
        }
    }
}
