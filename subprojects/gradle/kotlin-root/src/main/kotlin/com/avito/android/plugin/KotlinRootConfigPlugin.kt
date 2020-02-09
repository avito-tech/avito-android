package com.avito.android.plugin

import com.android.build.gradle.api.AndroidSourceSet
import com.avito.android.withAndroidModule
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.isRoot
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.internal.AndroidExtensionsExtension
import org.jetbrains.kotlin.gradle.internal.AndroidExtensionsFeature
import org.jetbrains.kotlin.gradle.internal.CacheImplementation
import org.jetbrains.kotlin.gradle.plugin.KaptExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * Глобальные настройки kotlin gradle plugin
 */
@Suppress("unused")
class KotlinRootConfigPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        require(target.isRoot()) { "KotlinRootConfigPlugin must be applied to root project" }

        val kaptMapDiagnosticLocations = target.getBooleanProperty("kaptMapDiagnosticLocations", false)
        val kaptBuildCache = target.getBooleanProperty("kaptBuildCache", true)
        val buildCache = target.gradle.startParameter.isBuildCacheEnabled

        target.subprojects { subProject ->
            setupParcelize(subProject)
            setupKapt(subProject, kaptMapDiagnosticLocations, buildCache, kaptBuildCache)

            subProject.plugins.withId("kotlin-android") {
                setupSourceSets(subProject)
                applyDefaultKotlinCompileOptions(subProject)
            }

            subProject.plugins.withId("kotlin") {
                // sourceSets тут правильно настроены по-умолчанию
                applyDefaultKotlinCompileOptions(subProject)
            }
        }
    }

    private fun setupSourceSets(target: Project) {
        target.withAndroidModule { android ->
            android.sourceSets {
                it.addKotlinSource("main")
                it.addKotlinSource("androidTest")
                it.addKotlinSource("test")
            }
        }
    }

    private fun NamedDomainObjectContainer<AndroidSourceSet>.addKotlinSource(sourceSet: String) {
        named(sourceSet).configure { it.java.srcDir("src/$sourceSet/kotlin") }
    }

    private fun setupParcelize(target: Project) {
        target.plugins.withId("kotlin-android-extensions") {
            target.extensions.getByType<AndroidExtensionsExtension>().run {
                defaultCacheImplementation = CacheImplementation.NONE
                features = setOf(AndroidExtensionsFeature.PARCELIZE.featureName)
            }
        }
    }

    private fun setupKapt(
        target: Project,
        kaptMapDiagnosticLocations: Boolean,
        buildCache: Boolean,
        kaptBuildCache: Boolean
    ) {
        target.plugins.withId("kotlin-kapt") {
            target.extensions.getByType<KaptExtension>().run {
                mapDiagnosticLocations = kaptMapDiagnosticLocations
                useBuildCache = buildCache && kaptBuildCache
            }
        }
    }

    private fun applyDefaultKotlinCompileOptions(target: Project) {
        target.tasks.withType<KotlinCompile> {
            kotlinOptions {
                // TODO: MBS-5681 ломает анализ bytecode
//                freeCompilerArgs = freeCompilerArgs + listOf(
//                    "-progressive"
//                )
                languageVersion = "1.3"
                apiVersion = "1.3"

                jvmTarget = "1.8"
                targetCompatibility = "1.8"
                sourceCompatibility = "1.8"
            }
        }
    }
}
