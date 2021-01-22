package com.avito.android.plugin

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.avito.android.isAndroid
import com.avito.android.prefetchTaskProvider
import com.avito.android.withAndroidModule
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

/**
 * To share the same robolectric settings across modules
 *
 * https://docs.gradle.org/current/userguide/sharing_build_logic_between_subprojects.html#sec:convention_plugins
 */
@Suppress("UnstableApiUsage")
class RobolectricConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {

        require(target.isAndroid()) { "Plugin must be applied to an android module but was applied to ${target.path}" }

        val extension = target.extensions.create<RobolectricConventionExtension>("robolectricConvention")

        val avitoRobolectricLibVersion = extension.avitoRobolectricLibVersion.convention(
            target.providers.gradleProperty("infraVersion")
        )

        target.afterEvaluate {
            target.withAndroidModule { android ->

                if (extension.wirePrefetchPlugin.get()) {
                    android.unitTestVariants.all {
                        val xTask = it.assembleProvider.get()
                        target.logger.lifecycle("Setting dependency ${xTask.path} on robolectric prefetch task")

                        it.assembleProvider.dependsOn(target.tasks.prefetchTaskProvider())
                    }
                }

                android.testOptions.unitTests.isIncludeAndroidResources = extension.includeAndroidResources.get()

                val targetConfiguration = extension.targetConfiguration.get()

                target.dependencies.add(
                    targetConfiguration,
                    "androidx.test:core:${extension.androidXTestVersion.get()}"
                )

                target.dependencies.add(
                    targetConfiguration,
                    "com.avito.android:robolectric:${avitoRobolectricLibVersion.get()}"
                )
            }
        }
    }
}
