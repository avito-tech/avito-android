package com.avito.jvm

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * JVM libraries consumed on the Android classpath: bytecode stays at Java 8 until every consumer
 * compiles with a higher `jvmTarget`; `--release`/`-Xjdk-release` keep newer JDK API out of the class files.
 */
class KotlinJvmAndroidPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            plugins.apply(KotlinJvmBasePlugin::class.java)
            tasks.withType(JavaCompile::class.java).configureEach {
                it.options.release.set(8)
            }

            tasks.withType(KotlinCompile::class.java).configureEach {
                it.compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_1_8)
                    freeCompilerArgs.add("-Xjdk-release=${JvmTarget.JVM_1_8.target}")
                }
            }
        }
    }
}
