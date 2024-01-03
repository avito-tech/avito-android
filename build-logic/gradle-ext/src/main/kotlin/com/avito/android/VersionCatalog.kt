package com.avito.android

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project

/**
 * workaround to make version catalog accessible in convention plugins
 * https://github.com/gradle/gradle/issues/15383
 */
fun Project.withVersionCatalog(block: (libs: LibrariesForLibs) -> Unit) {
    if (project.name != "gradle-kotlin-dsl-accessors") {
        val libs = extensions.getByType(LibrariesForLibs::class.java)
        block.invoke(libs)
    }
}
