package com.avito.instrumentation.impact

import java.io.File

public interface KotlinClassesFinder {

    public fun findClasses(file: File): Sequence<FullClassName>

    public companion object {

        public const val KOTLIN_FILE_EXTENSION: String = "kt"

        public fun create(): KotlinClassesFinder = KotlinClassesFinderImpl()
    }
}
