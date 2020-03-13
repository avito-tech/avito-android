package com.avito.android.test.report.performance

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

class PerformanceTestReporter {

    private val performanceMetrics = mutableListOf<Metric>()

    fun report(key: String, value: Double) {
        performanceMetrics.add(Metric(key, value))
    }

    fun getAsJson() = if (performanceMetrics.isNotEmpty()) {
        gson.toJson(performanceMetrics)
    } else {
        null
    }
}

private class Metric(val key: String, val value: Double)

private val gson: Gson by lazy {
    GsonBuilder()
        .registerTypeAdapter(Metric::class.java, MetricTypeAdapter())
        .create()
}

private class MetricTypeAdapter : TypeAdapter<Metric>() {
    override fun write(writer: JsonWriter, metric: Metric) {
        writer.beginObject()
        writer.name(metric.key)
        writer.value(metric.value)
        writer.endObject()
    }

    override fun read(reader: JsonReader): Metric {
        throw IllegalStateException()
    }
}
