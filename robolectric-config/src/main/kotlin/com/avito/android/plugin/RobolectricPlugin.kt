package com.avito.android.plugin

import com.avito.android.withAndroidModule
import com.avito.kotlin.dsl.getMandatoryStringProperty
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * To share the same robolectric settings across modules
 */
@Suppress("unused")
class RobolectricPlugin : Plugin<Project> {

    override fun apply(target: Project) {

        val androidXTestVersion = target.getMandatoryStringProperty("androidXTestVersion")

        target.dependencies.add("testImplementation", "androidx.test:core:$androidXTestVersion")
        target.dependencies.add("testImplementation", target.project(":test:utils:robolectric")) //todo open source

        target.withAndroidModule { extension ->
            extension.testOptions.unitTests.isIncludeAndroidResources = true
        }
    }
}
