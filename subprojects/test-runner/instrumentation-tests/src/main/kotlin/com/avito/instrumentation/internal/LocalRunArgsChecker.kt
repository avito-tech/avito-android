package com.avito.instrumentation.internal

import com.avito.instrumentation.dumpDirName
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File

internal class LocalRunArgsChecker(private val dumpDir: () -> File) {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    constructor(outputDirResolver: OutputDirResolver) : this(dumpDir = {
        outputDirResolver.resolveWithDeprecatedProperty()
            .map { it.dir(dumpDirName) }
            .get()
            .asFile
    })

    fun dumpArgs(args: Map<String, String>) {
        getDumpFile().writer().use { gson.toJson(args, it) }
    }

    @Suppress("unchecked_cast")
    fun readDump(): Map<String, String> {
        return gson.fromJson(getDumpFile().reader(), Map::class.java) as Map<String, String>
    }

    private fun getDumpFile(): File {
        return File(dumpDir.invoke(), "local-instrumentation-args-dump.json").apply {
            parentFile.mkdirs()
        }
    }
}
