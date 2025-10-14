package com.avito.android.contracts.platform.shared

import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import java.io.File

internal fun Project.findPackageDirectory(packageName: Provider<String>): Provider<Directory> {
    val packageFilePath = packageName.map { it.replace(".", File.separator) }
    return mainSourceSetDirectory().dir(packageFilePath)
}

internal fun Project.mainSourceSetDirectory(): Directory {
    val mainDirectory = project.layout.projectDirectory.dir("src")
        .dir("main")

    return mainDirectory.dir("java").takeIf { it.asFile.exists() }
        ?: mainDirectory.dir("kotlin")
}

internal fun Project.reportFile(directory: String, reportFileName: String): Provider<RegularFile> {
    return project.layout.buildDirectory
        .dir("reports")
        .map { it.dir(directory) }
        .map { it.file(reportFileName) }
}

/**
 * Extract scheme version from git branch name:
 *  * develop -> develop
 *  * release-avito/165.0 -> 165.0
 */
public fun extractSchemesVersionFromBranch(branchName: String): String {
    return branchName.split("/").last()
}
