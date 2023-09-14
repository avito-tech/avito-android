package com.avito.runner.service.worker.device.adb.request.adb

import com.avito.runner.service.worker.device.adb.request.AdbRequest

internal class LogcatAdbRequest(
    private val lines: Int?
) : AdbRequest() {

    override fun getArguments(): List<String> = buildList {
        add("logcat")
        if (lines != null) {
            add("-t")
            add("$lines")
        } else {
            add("-d")
        }
    }
}
