package com.avito.runner.service.worker.device.adb.request.shell

import com.avito.runner.service.worker.device.adb.request.AdbShellRequest

internal class RunTestsAdbShellRequest(
    private val testPackageName: String,
    private val testRunnerClass: String,
    private val instrumentationArguments: Map<String, String>,
    private val enableDeviceDebug: Boolean,
) : AdbShellRequest() {

    override fun getArguments(): List<String> = listOf(
        "am",
        "instrument",
        "-w", // wait for instrumentation to finish before returning.  Required for test runners.
        "-r", // raw mode is necessary for parsing
        "-e debug $enableDeviceDebug",
        instrumentationArguments.formatInstrumentationOptions(),
        "$testPackageName/$testRunnerClass",
    )

    private fun Map<String, String>.formatInstrumentationOptions(): String = when (isEmpty()) {
        true -> ""
        false -> " " + entries.joinToString(separator = " ") { "-e '${it.key}' '${it.value}'" }
    }
}
