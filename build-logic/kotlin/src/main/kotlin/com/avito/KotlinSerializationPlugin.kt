package com.avito

import com.avito.android.withVersionCatalog
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class KotlinSerializationPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            plugins.apply("org.jetbrains.kotlin.plugin.serialization")

            // workaround for https://github.com/gradle/gradle/issues/15383
            withVersionCatalog { libs ->
                dependencies.add("implementation", libs.kotlinx.serialization.json)
            }

            tasks.withType(KotlinCompile::class.java).configureEach {
                it.kotlinOptions {
                    freeCompilerArgs = freeCompilerArgs +
                        "-opt-in=kotlinx.serialization.ExperimentalSerializationApi"
                }
            }
        }
    }
}
