package com.avito.android.baseline_profile

import com.avito.android.baseline_profile.configuration.ApplyBaselineProfileExtension
import com.avito.android.baseline_profile.internal.ProfileTaskConfigurator
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

        extension.taskConfiguration.all { configuration ->
            val applicationProject = configuration.applicationModuleName
                .map { name ->
                    requireNotNull(target.rootProject.findProject(name)) {
                        "Could not resolve application project directory for ${configuration.applicationModuleName}"
                    }
                }

            ProfileTaskConfigurator(
                taskName = configuration.name,
                targetProject = target,
                applicationProject = applicationProject,
                rootProject = target.rootProject,
                configuration = configuration,
            ).configure()
        }
    }
}
