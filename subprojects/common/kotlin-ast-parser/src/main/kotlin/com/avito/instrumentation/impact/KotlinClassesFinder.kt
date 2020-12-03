package com.avito.instrumentation.impact

import java.io.File

interface KotlinClassesFinder {

    fun find(file: File): List<String>

    fun find(projectDir: File, relativePath: FilePath): List<Regex>
}
