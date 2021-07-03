package com.avito.impact.util

import java.io.File

public object AndroidManifestPackageParser {

    private val manifestPackagePattern = Regex(" package=\"(.*?)\"")

    public fun parse(manifestFile: File): String? {
        val packageLine = manifestFile.useLines { lines ->
            lines.find { manifestPackagePattern.containsMatchIn(it) }
        }

        return if (packageLine != null) {
            manifestPackagePattern.find(packageLine)?.groupValues?.get(1)
        } else {
            null
        }
    }
}
