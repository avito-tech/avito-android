package com.avito.jvm

import com.avito.android.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class KotlinJvmPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        with(project) {
            plugins.apply(KotlinJvmBasePlugin::class.java)
            val javaTarget = JavaLanguageVersion.of(libs.versions.javaTarget.get()).toString()

            tasks.withType(JavaCompile::class.java).configureEach {
                it.options.release.set(javaTarget.toInt())
            }

            tasks.withType(KotlinCompile::class.java).configureEach {
                it.compilerOptions {
                    jvmTarget.set(JvmTarget.fromTarget(javaTarget))
                    freeCompilerArgs.add("-Xjdk-release=$javaTarget")
                }
            }
        }
    }
}
