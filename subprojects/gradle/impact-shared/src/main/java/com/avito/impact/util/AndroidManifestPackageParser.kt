package com.avito.impact.util

import java.io.File

object AndroidManifestPackageParser {

    private val manifestPackagePattern = Regex(" package=\"(.*?)\"")

    fun parse(manifestFile: File): String? {
        val packageLine = manifestFile.useLines { lines ->
            lines.find {
                println(it)
                manifestPackagePattern.containsMatchIn(it) }
        }

        return if (packageLine != null) {
            manifestPackagePattern.find(packageLine)?.groupValues?.get(1)
        } else {
            null
        }
    }
}
