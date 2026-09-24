@file:Suppress("DEPRECATION")

package com.avito.android.tech_budget.internal.detekt

import com.android.build.api.variant.Component
import com.avito.android.tech_budget.internal.detekt.tasks.TechBudgetDetektTask
import com.avito.kotlin.dsl.getBooleanProperty
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.CustomDetektReport
import io.gitlab.arturbosch.detekt.extensions.DetektReports
import org.gradle.api.Action
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaToolchainService
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import java.io.File

private const val CSV_REPORT_ID = "CsvOutputReport"

public val DetektReports.csv: CustomDetektReport
    get() = custom.first { it.reportId == CSV_REPORT_ID }

public fun DetektReports.csv(configuration: Action<in CustomDetektReport>) {
    custom {
        configuration.execute(it)
        it.reportId = CSV_REPORT_ID
    }
}

internal fun Detekt.setupWithDefaults(block: Detekt.() -> Unit = {}) {
    parallel = true

    /**
     * About config:
     * yaml is a copy of https://github.com/detekt/detekt/blob/master/detekt-core/src/main/resources/default-detekt-config.yml
     * all rules are disabled by default, enabled one by one
     */
    config.setFrom(project.files(project.rootDir.resolve("detekt.yml")))

    buildUponDefaultConfig = false

    include("**/*.kt")
    include("**/*.kts")
    reports {
        it.xml.required.set(false)
        it.html.required.set(false)
        it.txt.required.set(true)
    }
    block.invoke(this)
}

internal fun TechBudgetDetektTask.enableAndroidTypeResolution(
    component: Component,
    bootClasspath: FileCollection
) {
    logger.lifecycle("Detekt applied for variant ${component.name}")

    val componentSources = project.files(
        listOfNotNull(component.sources.java?.all, component.sources.kotlin?.all)
    )

    val classpath = project.files(
        bootClasspath,
        projectClassesDirectories,
        projectClassesJars,
        component.compileClasspath.filter { it.exists() },
    )

    enableTypeResolution(
        sources = componentSources,
        classpath = classpath
    )
}

internal fun Detekt.enableKotlinTypeResolution(compilation: KotlinCompilation<*>) {
    // see io.gitlab.arturbosch.detekt.internal.registerDetektTask
    project.plugins.withType(JavaBasePlugin::class.java) {
        val toolchain = project.extensions.getByType(JavaPluginExtension::class.java).toolchain

        // acquire a provider that returns the launcher for the toolchain
        val service = project.extensions.getByType(JavaToolchainService::class.java)
        val defaultLauncher = service.launcherFor(toolchain)
        jdkHome.convention(defaultLauncher.map { launcher -> launcher.metadata.installationPath })
    }

    val sources = compilation.kotlinSourceSets.flatMap { it.kotlin.sourceDirectories }

    val classpath = compilation.output.classesDirs + compilation.compileDependencyFiles

    enableTypeResolution(
        sources = project.files(sources),
        classpath = project.files(classpath),
    )
}

internal fun Detekt.enableTypeResolution(
    sources: FileCollection,
    classpath: FileCollection,
) {
    detektClasspath.setFrom(project.configurations.getByName("detekt"))
    pluginClasspath.setFrom(project.configurations.getByName("detektPlugins"))

    setSource(sources)
    // For detekt type resolution we need to set up the classpath of
    // the project to create binding context inside the analyser.
    // This implementation is similar to the Detekt source code in [DetektAndroid].
    // You can find more details in the documentation: https://detekt.dev/docs/gettingstarted/type-resolution/
    this.classpath.setFrom(classpath)
    val rootProject = project.rootProject
    val parentDir = File(rootProject.buildDir.absoluteFile, "reports/detekt")
    reportsDir.set(parentDir)

    val reportFileName = "report$path.csv".replace(":", "_")
    val csvReportFile = File(parentDir, reportFileName)

    reports.csv {
        it.outputLocation.set(csvReportFile)
    }

    ignoreFailures = project.getBooleanProperty("com.avito.android.detekt.ignoreFailures", false)
}
