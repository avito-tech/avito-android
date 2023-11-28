package com.avito.android.network_contracts.utils

import org.gradle.api.Project
import org.gradle.api.file.Directory
import java.io.File

internal fun Project.findPackageDirectory(packageName: String): Directory {
    val packageFilePath = packageName.replace(".", "/")
    return mainSourceSetDirectory().dir(packageFilePath)
}

internal fun Project.mainSourceSetDirectory(): Directory {
    val srcFile = project.file("src/main/java").takeIf(File::exists) ?: project.file("src/main/kotlin")
    return project.layout.projectDirectory.dir(srcFile.toRelativeString(project.projectDir))
}
