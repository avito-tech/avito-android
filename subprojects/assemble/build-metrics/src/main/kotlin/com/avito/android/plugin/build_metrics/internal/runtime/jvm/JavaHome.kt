package com.avito.android.plugin.build_metrics.internal.runtime.jvm

import java.io.File

internal class JavaHome(
    val path: File = File(System.getProperty("java.home"))
) {
    val isJdk: Boolean by lazy {
        File(path, "bin/jps").exists()
    }
}
