package com.avito.instrumentation.impact

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

internal fun symbolListWithPackageNamePath(projectDir: File, testBuildType: String): File =
    Paths.get(
        projectDir.path,
        "build",
        "intermediates",
        "symbol_list_with_package_name",
        testBuildType,
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
