package com.avito.instrumentation.impact

import java.io.File

interface KotlinClassesFinder {

    fun findClasses(file: File): Sequence<FullClassName>
}
