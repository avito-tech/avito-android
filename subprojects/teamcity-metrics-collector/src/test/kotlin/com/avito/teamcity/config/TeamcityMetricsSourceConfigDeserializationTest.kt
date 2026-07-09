package com.avito.teamcity.config

import com.avito.teamcity.model.TeamcityMetricsSource
import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TeamcityMetricsSourceConfigDeserializationTest {

    @Test
    fun deserialization() {
        val configJson = """
            |{
            |   "sources": [
            |       {"configurationId": "configuration1", "fetchIntervalInHours": "3", "metricsPrefix": "some.namespace"},
            |       {"configurationId": "configuration2", "fetchIntervalInHours": "10", "metricsPrefix": "other.namespace"}
            |   ]
            |}
        """.trimMargin()
        val config = Json.decodeFromString<TeamcityMetricsSourceConfig>(configJson)
        assertThat(config).isEqualTo(TeamcityMetricsSourceConfig(
            sources = listOf(
                TeamcityMetricsSource("configuration1", 3, metricsPrefix = "some.namespace"),
                TeamcityMetricsSource("configuration2", 10, metricsPrefix = "other.namespace"),
            )
        ))
    }

    @Test
    fun `source without metricsPrefix - deserialization fails`() {
        val configJson = """
            |{
            |   "sources": [
            |       {"configurationId": "configuration1", "fetchIntervalInHours": "3"}
            |   ]
            |}
        """.trimMargin()
        assertThrows<SerializationException> {
            Json.decodeFromString<TeamcityMetricsSourceConfig>(configJson)
        }
    }
}
