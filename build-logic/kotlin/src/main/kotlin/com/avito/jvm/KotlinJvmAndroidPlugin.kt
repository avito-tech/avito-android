package com.avito.jvm

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class KotlinJvmAndroidPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            plugins.apply(KotlinJvmBasePlugin::class.java)
            tasks.withType(JavaCompile::class.java) {
                it.sourceCompatibility = "1.8"
                it.targetCompatibility = "1.8"
            }

            tasks.withType(KotlinCompile::class.java).configureEach {
                it.kotlinOptions {
                    jvmTarget = "1.8"
                }
            }
        }
    }
}
