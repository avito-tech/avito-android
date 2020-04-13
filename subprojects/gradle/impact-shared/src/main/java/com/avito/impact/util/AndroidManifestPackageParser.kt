package com.avito.impact.util

import java.io.File

object AndroidManifestPackageParser {

    private val manifestPackagePattern = Regex(" package=\"([a-zA-Z0-9._]+)\"")

    fun parse(manifestFile: File): String? {
        val manifest = manifestFile.readText()
        return manifestPackagePattern.find(manifest)?.groupValues?.get(1)
    }
}
