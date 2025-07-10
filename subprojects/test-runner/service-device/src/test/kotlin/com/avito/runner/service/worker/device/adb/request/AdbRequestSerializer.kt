package com.avito.runner.service.worker.device.adb.request

import java.util.StringJoiner

internal class AdbRequestSerializer(private val deviceSerial: String) {

    /**
     * This implementation is copied from the [java.lang.ProcessBuilder.start], which is used to execute shell requests.
     * Also,the [com.avito.cli.CommandLine] separates the command and arguments in its constructor
     * but then merges them into a single list and passes it to the [java.lang.ProcessBuilder].
     */
    fun serialize(adbRequest: AdbRequest): String {
        val command = StringJoiner(" ")
        for (string in listOf("adb") + adbRequest.serialize(deviceSerial = deviceSerial)) {
            command.add(string)
        }
        return command.toString()
    }
}
