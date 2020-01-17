package com.avito.performance

import com.avito.utils.createOrClear
import com.google.gson.Gson
import java.io.File

internal class PerformanceWriter {

    private val gson = Gson()

    fun write(tests: Any, output: File) {
        output.createOrClear()
        output.writeText(gson.toJson(tests))
    }
}
