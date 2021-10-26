package com.avito.instrumentation.internal

import com.avito.runner.config.RunnerInputParams
import com.avito.utils.gradle.KubernetesCredentials
import com.google.gson.GsonBuilder
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

internal class RunnerInputDumper(private val dumpDir: File) {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    fun readInput(): RunnerInputParams {
        return ObjectInputStream(getDumpFile().inputStream()).use {
            it.readObject() as RunnerInputParams
        }
    }

    fun dumpInput(input: RunnerInputParams, isGradleTestKitRun: Boolean) {
        val safeInput = if (isGradleTestKitRun) {
            input
        } else {
            excludeSensitiveInfo(input)
        }

        // write this file always, for troubleshooting purposes
        getDumpJson().writer().use {
            gson.toJson(safeInput, it)
        }

        if (isGradleTestKitRun) {
            ObjectOutputStream(getDumpFile().outputStream()).use {
                it.writeObject(safeInput)
            }
        }
    }

    private fun excludeSensitiveInfo(input: RunnerInputParams): RunnerInputParams {
        return input.copy(kubernetesCredentials = KubernetesCredentials.Empty)
    }

    /**
     * used in tests for easier serialization
     */
    private fun getDumpFile(): File {
        return File(dumpDir, "instrumentation-extension-dump.bin").apply {
            parentFile.mkdirs()
        }
    }

    /**
     * human readable
     */
    private fun getDumpJson(): File {
        return File(dumpDir, "test-runner-args-dump.json").apply {
            parentFile.mkdirs()
        }
    }
}
