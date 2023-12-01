package com.avito.android.baseline_profile.internal

import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import java.io.File

internal object BaselineProfileFileLocationExtensions {
    private const val baselineProfileFileName = "baseline-prof.txt"

    internal fun Project.baselineProfileTargetLocation(): RegularFile = layout.projectDirectory
        .dir("src")
        .dir("main")
        .file(baselineProfileFileName)

    internal fun Directory.findProfileOrThrow(): File {
        val fileFoundByNamePattern = asFile
            .listFiles()
            ?.find { it.name.endsWith(baselineProfileFileName) }
        return requireNotNull(fileFoundByNamePattern) {
            """Could not locate baseline profile while searching in outputs.
                 directory - ${asFile.absolutePath}"
                 contents - ${asFile.listFiles()?.map { it.name } ?: emptyList()}
            """
        }
    }
}
