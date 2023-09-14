package com.avito.runner.service.worker.device.adb.request.shell

import com.avito.runner.service.worker.device.adb.request.AdbShellRequest
import java.nio.file.Path

internal class ListDirectoryAdbShellRequest(
    private val remotePath: Path,
) : AdbShellRequest() {

    override fun getArguments(): List<String> = listOf(
        "ls",
        remotePath.toString(),
    )
}
