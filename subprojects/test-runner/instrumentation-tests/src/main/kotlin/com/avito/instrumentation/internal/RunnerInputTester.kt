package com.avito.instrumentation.internal

import com.avito.runner.config.InstrumentationTestsActionParams
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

internal object RunnerInputTester {

    private const val instrumentationDumpPath: String = "instrumentation-extension-dump.bin"

    fun readInput(rootDir: File): InstrumentationTestsActionParams {
        return ObjectInputStream(getDumpFile(rootDir).inputStream()).use {
            it.readObject() as InstrumentationTestsActionParams
        }
    }

    fun dumpInput(rootDir: File, input: InstrumentationTestsActionParams) {
        ObjectOutputStream(getDumpFile(rootDir).outputStream()).use {
            it.writeObject(input)
        }
    }

    private fun getDumpFile(rootBuildDir: File): File {
        return File(rootBuildDir, instrumentationDumpPath)
    }
}
