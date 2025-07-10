package com.avito.runner.service.worker.device.adb.request.shell

import com.avito.runner.service.worker.device.adb.request.AdbShellRequest
import java.nio.file.Path

internal class ClearDirectoryAdbShellRequest(
    private val path: Path,
) : AdbShellRequest() {

    override fun getArguments(): List<String> = listOf(
        "rm",
        "-rf",
        path.toString(),
    )
}
