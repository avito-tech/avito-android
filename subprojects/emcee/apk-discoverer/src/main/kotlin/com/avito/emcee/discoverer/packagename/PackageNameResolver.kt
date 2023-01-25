package com.avito.emcee.discoverer.packagename

import java.lang.RuntimeException
import java.nio.file.Path
import java.util.concurrent.TimeUnit

internal class PackageNameResolver {

    fun resolve(path: Path): PackageNameResult {
        val process = ProcessBuilder(
            "/opt/android-sdk/build-tools/32.0.0/aapt2",
            "dump",
            "packagename",
            path.toString(),
        ).redirectErrorStream(true).start()

        var packageName: String
        process.inputStream.reader().use {
            packageName = it.readText().trim()
        }
        process.waitFor(10, TimeUnit.SECONDS)
        if (process.exitValue() != 0) throw RuntimeException("Cannot read package name")
        return PackageNameResult(packageName)
    }
}
