package com.avito.android.lint.util

import java.io.File
import java.util.jar.JarFile
import java.util.zip.ZipException

internal class JarContent() {

    /**
     * Collection of [java.util.zip.ZipEntry.name]
     */
    private lateinit var entryNames: List<String>

    constructor(jar: File) : this() {
        entryNames = if (jar.exists()) {
            val names = mutableListOf<String>()
            try {
                JarFile(jar).use { jarFile ->
                    jarFile.entries().asSequence()
                        .forEach { entry ->
                            names.add(entry.name)
                        }
                }
            } catch (ignored: ZipException) {
                println("Skip invalid file " + jar.path)
            }
            names
        } else {
            emptyList()
        }
    }

    /**
     * Fully qualified class names with '/' package separators
     */
    val classes: Set<String> by lazy {
        entryNames.asSequence()
            .filter { it.endsWith(".class") }
            .filterNot { it.isSynthetic() }
            .map { it.substringBeforeLast(".class") }
            .toSet()
    }

    private fun String.isSynthetic(): Boolean {
        // TODO: it != /_\$\w+\.class$/)
        return false
    }
}
