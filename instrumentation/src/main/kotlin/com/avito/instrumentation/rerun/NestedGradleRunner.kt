package com.avito.instrumentation.rerun

import org.funktionale.tries.Try
import java.io.File

interface NestedGradleRunner {

    fun run(
        workingDirectory: File,
        tasks: List<String>,
        buildScan: Boolean,
        jvmArgs: String,
        workers: Int,
        projectParams: Map<String, String>
    ): Try<Unit>
}
