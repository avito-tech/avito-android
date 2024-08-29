package com.avito.runner.model

import java.io.Serializable

/**
 * TODO move from configuration time, because it breaks gradle kotlin-dsl resolution (MBS-10591)
 */
public data class InstrumentationParameters(
    val initialParameters: Map<String, String> = emptyMap()
) : Map<String, String> by initialParameters, Serializable {

    init {
        validateParameters(initialParameters)
    }

    public fun applyParameters(newParameters: Map<String, String>): InstrumentationParameters {
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
}
