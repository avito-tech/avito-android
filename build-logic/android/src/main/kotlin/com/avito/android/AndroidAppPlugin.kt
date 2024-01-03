package com.avito.android

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariantBuilder
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidAppPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            plugins.apply("com.android.application")
            plugins.apply(AndroidBasePlugin::class.java)
            plugins.apply(KotlinAndroidBasePlugin::class.java)
            plugins.apply("convention.dependency-locking-android-app")
            val buildType = "debug"

            extensions.configure(AndroidComponentsExtension::class.java) {
                it.beforeVariants { variants ->
                    variants as ApplicationVariantBuilder
                    /**
                     * Disable all buildTypes except testing
                     * to avoid confusing errors in IDE if wrong build variant is selected
                     */
                    if (variants.buildType != buildType) {
                        variants.enable = false
                        logger.lifecycle("Build variant ${variants.name} is omitted for module: $path")
                    }
                }
            }
            @Suppress("UnstableApiUsage")
            extensions.configure(ApplicationExtension::class.java) { android ->
                with(android) {
                    testBuildType = buildType
                    buildTypes {
                        getByName(testBuildType) {
                            // libraries only built in release variant, see convention.kotlin-android-library
                            it.matchingFallbacks += listOf("release")
                        }
                    }
                }
            }
        }
    }
}
