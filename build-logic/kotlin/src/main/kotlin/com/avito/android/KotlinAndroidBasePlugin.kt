package com.avito.android

import com.avito.KotlinBasePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class KotlinAndroidBasePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        with(project) {
            plugins.apply("kotlin-android")
            plugins.apply(KotlinBasePlugin::class.java)
            extensions.configure(KotlinAndroidProjectExtension::class.java) { kotlin ->
                kotlin.explicitApi()
                kotlin.jvmToolchain {
                    it.languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
                }
            }

            val javaTarget = libs.versions.javaTarget.get()
            tasks.withType(KotlinCompile::class.java).configureEach {
                it.compilerOptions {
                    jvmTarget.set(JvmTarget.fromTarget(javaTarget))
                }
            }
        }
    }
}
