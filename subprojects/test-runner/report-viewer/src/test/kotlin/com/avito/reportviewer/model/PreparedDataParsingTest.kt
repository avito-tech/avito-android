package com.avito.reportviewer.model

import com.avito.report.serialize.createReportGson
import com.avito.reportviewer.internal.model.PreparedData
import com.github.salomonbrys.kotson.fromJson
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class PreparedDataParsingTest {

    private val gson = createReportGson()

    @Test
    fun `deserialize - integer run duration - parsed as float`() {
        val json = """
            {
                "verdict": "passed",
                "run_duration": 42,
                "error_hash": "hash",
                "tc_build": "build"
            }
        """.trimIndent()

        val result = gson.fromJson<PreparedData>(json)

        assertThat(result.runDuration).isEqualTo(42f)
    }

    @Test
    fun `deserialize - float run duration - parsed as float`() {
        val json = """
            {
                "verdict": "passed",
                "run_duration": 42.5,
                "error_hash": "hash",
                "tc_build": "build"
            }
        """.trimIndent()

        val result = gson.fromJson<PreparedData>(json)

        assertThat(result.runDuration).isEqualTo(42.5f)
    }
}
