package com.avito.android

import org.gradle.api.Project
import org.gradle.api.file.Directory
import java.io.File

/**
 * projectDir of includedBuild available only in java.io.File form
 * Using ProjectLayout getting org.gradle.api.file.Directory from it
 */
fun Project.resolveDir(dir: File): Directory {
    return layout.projectDirectory.dir(dir.relativeTo(projectDir).path)
}
