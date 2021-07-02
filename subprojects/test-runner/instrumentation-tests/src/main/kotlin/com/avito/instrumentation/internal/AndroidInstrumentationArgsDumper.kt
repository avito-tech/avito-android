package com.avito.instrumentation.internal

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File

internal class AndroidInstrumentationArgsDumper(private val dumpDir: File) {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    fun dumpArgs(args: Map<String, String>) {
        getDumpFile().writer().use { gson.toJson(args, it) }
    }

    @Suppress("unchecked_cast")
    fun readDump(): Map<String, String> {
        return gson.fromJson(getDumpFile().reader(), Map::class.java) as Map<String, String>
    }

    private fun getDumpFile(): File {
        return File(dumpDir, "local-instrumentation-args-dump.json")
    }
}
