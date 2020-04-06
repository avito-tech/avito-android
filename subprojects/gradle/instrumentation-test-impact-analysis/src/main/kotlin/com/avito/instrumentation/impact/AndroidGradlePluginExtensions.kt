package com.avito.instrumentation.impact

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.LibraryVariant
import com.avito.android.androidLibraryExtension
import org.gradle.api.Project
import java.io.File
import java.nio.file.Paths

internal fun runtimeSymbolListPath(projectDir: File, testBuildType: String): File =
    Paths.get(
        projectDir.path,
        "build",
        "intermediates",
        "runtime_symbol_list",
        testBuildType,
        "R.txt"
    ).toFile()

internal fun symbolListWithPackageNamePath(projectDir: File, variantName: String): File =
    Paths.get(
        projectDir.path,
        "build",
        "intermediates",
        "symbol_list_with_package_name",
        variantName,
        "package-aware-r.txt"
    ).toFile()

internal fun mergedAssetsPath(projectDir: File, testBuildType: String): File =
    Paths.get(
        projectDir.path,
        "build",
        "intermediates",
        "merged_assets",
        "${testBuildType}AndroidTest",
        "out"
    ).toFile()

internal fun getLibraryBuildVariant(app: AppExtension, library: Project, appVariant: String): String {
    val asIsVariant = findLibraryVariant(library, appVariant)
    if (asIsVariant != null) {
        return asIsVariant.name
    } else {
        val fallbacks = app.buildTypes.named(app.testBuildType).get().matchingFallbacks
        fallbacks.forEach {
            val variant = findLibraryVariant(library, it)
            if (variant != null) {
                return variant.name
            }
        }
    }
    error("No matching variant found for library: ${library.path} and appVariant: $appVariant")
}

internal fun findLibraryVariant(library: Project, variant: String): LibraryVariant? {
    return library.androidLibraryExtension.libraryVariants.find { it.name == variant }
}
