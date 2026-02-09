package com.avito.i18n.plugin

import com.avito.android.androidBaseExtension
import org.gradle.api.Project
import java.io.File
import java.util.Locale

internal val Project.mainResDir: File?
    get() = androidBaseExtension.sourceSets.getByName("main").res.srcDirs.firstOrNull()

internal fun Project.getResDirByName(name: String): File? =
    androidBaseExtension.sourceSets.getByName(name).res.srcDirs.firstOrNull()

internal fun Locale.getStringsFile(): String {
    return "values-${getResourceQualifier()}/strings.xml"
}

private fun Locale.getResourceQualifier(): String {
    return if (country.isNotBlank()) {
        "$language-r$country"
    } else {
        language
    }
}
