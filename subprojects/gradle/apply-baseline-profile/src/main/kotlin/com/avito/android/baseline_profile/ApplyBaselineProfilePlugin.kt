package com.avito.android.baseline_profile

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

/**
 * Registers task which applies baseline profile using following steps:
 * 1. assembles apk using module and variant name provided with extension
 * 2. launches instrumentation test against apk to generate baseline profile
 * 3. copies generated file to source files (app/src/main) profile would be applied during next assemble
 * 4. optionally pushes file into version control
 */
public abstract class ApplyBaselineProfilePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions
            .create<ApplyBaselineProfileExtension>("applyBaselineProfile")

        // afterEvaluate is required to resolve instrumentation task
        target.afterEvaluate {
            extension.validateValues()

            val applicationProject = extension.applicationModuleName
                .map { name ->
                    requireNotNull(target.rootProject.findProject(name)) {
                        "Could not resolve application project directory for ${extension.applicationModuleName}"
                    }
                }

            ProfileTaskConfigurator(
                targetProject = target,
                applicationProject = applicationProject,
                rootProject = target.rootProject,
                extension = extension,
            ).configure()
        }
    }
}
