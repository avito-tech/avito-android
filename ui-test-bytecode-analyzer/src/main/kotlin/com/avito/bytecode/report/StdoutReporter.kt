package com.avito.bytecode.report

import com.google.gson.Gson
import com.google.gson.GsonBuilder

class StdoutReporter(
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
) : DataReporter {

    override fun <T> report(data: T) {
        println(gson.toJson(data))
    }
}
