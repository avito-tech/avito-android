package com.avito.instrumentation.internal

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import java.io.File

internal class AndroidInstrumentationArgsDumper(private val dumpDir: File) {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    constructor(dumpDir: Provider<Directory>) : this(dumpDir.get().asFile)

    fun dumpArgs(args: Map<String, String>) {
        getDumpFile().writer().use { gson.toJson(args, it) }
    }

    @Suppress("unchecked_cast")
    fun readDump(): Map<String, String> {
        return gson.fromJson(getDumpFile().reader(), Map::class.java) as Map<String, String>
    }

    private fun getDumpFile(): File {
        return File(dumpDir, "local-instrumentation-args-dump.json").apply {
            parentFile.mkdirs()
        }
    }
}
