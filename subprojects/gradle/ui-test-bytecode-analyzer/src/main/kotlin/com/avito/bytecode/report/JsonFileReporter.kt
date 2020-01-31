package com.avito.bytecode.report

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File

class JsonFileReporter(
    private val path: File,
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
) : DataReporter {

    override fun <T> report(data: T) {
        path.writeText(
            gson.toJson(data)
        )
    }
}
