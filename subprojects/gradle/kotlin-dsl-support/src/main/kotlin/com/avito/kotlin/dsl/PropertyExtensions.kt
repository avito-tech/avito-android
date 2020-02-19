@file:Suppress("UnstableApiUsage")

package com.avito.kotlin.dsl

import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.internal.provider.Providers
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import java.io.File

/**
 * https://github.com/gradle/gradle/issues/2016#issuecomment-492598038
 */
fun Property<RegularFile>.optionalIfNotExists(): Provider<RegularFile?> =
    flatMap {
        if (it.asFile.exists() && it.asFile.length() > 0) {
            Providers.of(it)
        } else {
            Providers.notDefined()
        }
    }

fun Project.optionalIfNotExists(file: File): Provider<RegularFile?> =
    objects.fileProperty().apply { set(file) }.optionalIfNotExists()
