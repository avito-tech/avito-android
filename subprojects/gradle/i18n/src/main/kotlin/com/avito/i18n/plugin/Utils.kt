package com.avito.i18n.plugin

import com.avito.android.androidBaseExtension
import org.gradle.api.Project
import java.io.File

internal val Project.mainResDir: File?
    get() = androidBaseExtension.sourceSets.getByName("main").res.srcDirs.firstOrNull()

internal fun String.getValuesDir(): String {
    if (isBlank()) {
        return "values"
    }
    val list = split("-")
    val lang = list[0].ifBlank { "" }
    val region = if (list.size > 1) {
        "-r${list[1]}"
    } else {
        ""
    }
    return "values-$lang$region"
}

internal fun String.getStringsFile(): String {
    return getValuesDir() + "/strings.xml"
}
