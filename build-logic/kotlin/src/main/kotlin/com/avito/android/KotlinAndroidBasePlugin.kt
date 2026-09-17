package com.avito.android

import com.avito.KotlinBasePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

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
        }
    }
}
