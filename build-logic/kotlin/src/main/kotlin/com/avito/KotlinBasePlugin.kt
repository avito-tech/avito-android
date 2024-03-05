package com.avito

import com.avito.android.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class KotlinBasePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        with(project) {
            plugins.apply("convention.detekt")
            plugins.apply("convention.unit-testing")
            /**
             * Exists because `compile` task ambiguous in projects with jvm and android modules combined
             */
            tasks.register("compileAll") {
                it.description = "Compiles all available modules in all variants"
                it.dependsOn(tasks.withType(KotlinCompile::class.java))
            }

            // workaround for https://github.com/gradle/gradle/issues/15383
            val kotlinLanguageVersion = libs.versions.kotlinLanguageVersion.get()
            tasks.withType(KotlinCompile::class.java).configureEach {
                it.kotlinOptions {
                    allWarningsAsErrors = true
                    languageVersion = kotlinLanguageVersion
                    apiVersion = kotlinLanguageVersion

                    freeCompilerArgs = freeCompilerArgs +
                        "-opt-in=kotlin.RequiresOptIn" +
                        "-progressive"
                }
            }
        }
    }
}
