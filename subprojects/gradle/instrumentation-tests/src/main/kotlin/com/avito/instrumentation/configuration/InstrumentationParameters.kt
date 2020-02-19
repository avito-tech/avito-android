package com.avito.instrumentation.configuration

import com.avito.report.model.ReportCoordinates
import java.io.Serializable

data class InstrumentationParameters(
    val initialParameters: Map<String, String> = emptyMap()
) : Map<String, String> by initialParameters, Serializable {

    init {
        validateParameters(initialParameters)
    }

    fun applyParameters(newParameters: Map<String, String>): InstrumentationParameters {
        validateParameters(newParameters)
        return InstrumentationParameters(
            HashMap(this).apply {
                putAll(newParameters)
            }
        )
    }

    private fun validateParameters(parameters: Map<String, String>) {
        parameters.forEach { (key, value) ->
            require(key.isNotBlank() && value.isNotBlank()) {
                "pair key=$key, value=$value has blank string"
            }
        }
    }

    fun reportCoordinates(): ReportCoordinates = ReportCoordinates(
        planSlug = getValue("planSlug"),
        jobSlug = getValue("jobSlug"),
        runId = getValue("runId")
    )
}
