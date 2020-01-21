package com.avito.android

import org.gradle.api.Project
import java.io.File

/**
 * To be used in android gradle plugin configuration
 *
 * Example:
 * defaultConfig {
 *  ...
 *  proguardFiles(*proguardFromDir("proguard/common")) // path relative to projectDir
 * }
 */
fun Project.proguardFromDir(dir: String): Array<File> =
    fileTree(dir)
        .apply { include("*.pro") }
        .toList()
        .toTypedArray()
