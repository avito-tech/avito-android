package com.avito.jvm

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * JVM libraries consumed on the Android classpath (test runner, device-side helpers).
 * Bytecode stays at Java 8 until every consumer (avito-android release branches, AndroidWorker)
 * compiles with a higher `jvmTarget`; `--release`/`-Xjdk-release` keep JDK 9+ API of the JDK 21 toolchain
 * out of the class files, the same way `convention.kotlin-jvm` does for `javaTarget`.
 */
class KotlinJvmAndroidPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            plugins.apply(KotlinJvmBasePlugin::class.java)
            tasks.withType(JavaCompile::class.java).configureEach {
                it.options.release.set(JAVA_TARGET)
            }

            tasks.withType(KotlinCompile::class.java).configureEach {
                it.compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_1_8)
                    freeCompilerArgs.add("-Xjdk-release=${JvmTarget.JVM_1_8.target}")
                }
            }

            registerPublishedJvmVersionGuard(expectedJvmVersion = JAVA_TARGET.toString())
        }
    }

    private companion object {
        const val JAVA_TARGET = 8
    }
}
