@file:Suppress("DEPRECATION")

package com.avito.android.tech_budget.internal.detekt

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.LibraryVariant
import com.android.build.gradle.api.TestVariant
import com.android.build.gradle.api.UnitTestVariant
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

internal fun Detekt.enableAndroidTypeResolution(
    variant: BaseVariant,
    additionalVariantSources: FileCollection?,
    bootClasspath: FileCollection
) {
    logger.lifecycle("Detekt applied for variant ${variant.name}")

    val javaDestinations = project.files(
        variant.javaCompileProvider?.map { it.destinationDirectory }
    )

    val variantClasspath = project.files(
        variant.getCompileClasspath(null)?.filter { it.exists() }
    )

    @Suppress("DEPRECATION")
    fun BaseVariant.extractSources(): Collection<File> {
        return sourceSets.flatMap { it.javaDirectories + it.kotlinDirectories }
    }

    val variantSources = project.files(variant.extractSources())

    val classpath = project.files(
        bootClasspath,
        javaDestinations,
        variantClasspath,
    )

    enableTypeResolution(
        sources = additionalVariantSources?.plus(variantSources) ?: variantSources,
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

/**
 * Retrieves the base variant associated with a given variant.
 *
 * When called on a variant, this property will return:
 * - The base variant (like `debug`, `release`)
 * if the variant is a test variant (e.g., `UnitTestVariant`, `TestVariant`).
 * - The variant itself in all other cases.
 *
 * For example:
 *  * When called on a `UnitTestVariant` like `debugUnitTest`, `baseVariant` will return the `debug` variant.
 *  * When called on a non-test variant like `release`, it will return `release` itself.
 *
 * @return The base variant associated with the current variant.
 *     It returns the variant itself if it's not a test variant.
 */
internal val BaseVariant.baseVariant: BaseVariant
    get() {
        return when {
            this is UnitTestVariant && testedVariant is BaseVariant -> testedVariant as BaseVariant
            this is TestVariant -> testedVariant
            else -> this
        }
    }

internal val BaseVariant.unitTestVariant: UnitTestVariant?
    get() = when (this) {
        is ApplicationVariant -> unitTestVariant
        is LibraryVariant -> unitTestVariant
        else -> null
    }
