package com.avito.android.build_checks.internal

import com.avito.android.AndroidSdk
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.utils.gradle.buildEnvironment
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion

internal class BuildEnvLogger(
    private val project: Project,
    private val envInfo: BuildEnvironmentInfo,
) {

    fun log() {
        val isBuildCachingEnabled = project.gradle.startParameter.isBuildCacheEnabled
        val minSdk = project.getOptionalStringProperty("minSdk")
        val kaptBuildCache: Boolean = project.getBooleanProperty("kaptBuildCache")
        val kaptMapDiagnosticLocations = project.getBooleanProperty("kaptMapDiagnosticLocations")
        val javaIncrementalCompilation = project.getBooleanProperty("javaIncrementalCompilation")

        project.logger.info(
            """Config information for project: ${project.displayName}:
BuildEnvironment: ${project.buildEnvironment}
${startParametersDescription(project.gradle)}
java=${envInfo.javaInfo}
JAVA_HOME=${envInfo.javaHome}
ANDROID_HOME=${AndroidSdk.fromProject(project.rootDir).androidHome}
org.gradle.caching=$isBuildCachingEnabled
android.enableD8=${project.getOptionalStringProperty("android.enableD8")}
android.enableR8.fullMode=${project.getOptionalStringProperty("android.enableR8.fullMode")}
android.builder.sdkDownload=${project.getOptionalStringProperty("android.builder.sdkDownload")}
kotlin.version=${project.getKotlinPluginVersion()}
kotlin.incremental=${project.getOptionalStringProperty("kotlin.incremental")}
minSdk=$minSdk
preDexLibrariesEnabled=${project.getOptionalStringProperty("preDexLibrariesEnabled")}
kaptBuildCache=$kaptBuildCache
kapt.use.worker.api=${project.getOptionalStringProperty("kapt.use.worker.api")}
kapt.incremental.apt=${project.getOptionalStringProperty("kapt.incremental.apt")}
kapt.include.compile.classpath=${project.getOptionalStringProperty("kapt.include.compile.classpath")}
kaptMapDiagnosticLocations=$kaptMapDiagnosticLocations
javaIncrementalCompilation=$javaIncrementalCompilation
------------------------"""
        )
    }

    private fun startParametersDescription(gradle: Gradle): String =
        gradle.startParameter.toString().replace(',', '\n')
}
