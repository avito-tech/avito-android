package com.avito.runner.service.worker.device.adb.request.adb

import com.avito.runner.service.worker.device.adb.request.AdbRequest
import java.nio.file.Path

internal class PullAdbRequest(
    private val from: Path,
    private val to: Path,
) : AdbRequest() {

    override fun getArguments(): List<String> = listOf(
        "pull",
        from.toString(),
        to.toString(),
    )
}
