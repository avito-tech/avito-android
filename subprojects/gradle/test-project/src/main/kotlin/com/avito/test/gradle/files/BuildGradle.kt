package com.avito.test.gradle.files

import com.avito.test.gradle.file
import java.io.File

internal fun File.build_gradle(mutator: File.() -> Unit = {}) = file("build.gradle").apply(mutator)

internal fun File.build_gradle_kts(mutator: File.() -> Unit = {}) = file("build.gradle.kts").apply(mutator)
