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
    fun `source with sla fields - deserialization - exposes sla config, other sources have none`() {
        val configJson = """
            |{
            |   "sources": [
            |       {
            |           "configurationId": "configuration1", "fetchIntervalInHours": "3", "metricsPrefix": "some.namespace",
            |           "sla": {"thresholdMinutes": 90, "metricsPrefix": "some.sla.speed"}
            |       },
            |       {"configurationId": "configuration2", "fetchIntervalInHours": "10", "metricsPrefix": "other.namespace"}
            |   ]
            |}
        """.trimMargin()
        val config = Json.decodeFromString<TeamcityMetricsSourceConfig>(configJson)
        assertThat(config.sources[0].sla).isEqualTo(
            TeamcityMetricsSource.SlaConfig(thresholdMinutes = 90, metricsPrefix = "some.sla.speed")
        )
        assertThat(config.sources[1].sla).isNull()
    }

    @Test
    fun `sla without metricsPrefix - deserialization fails`() {
        val configJson = """
            |{
            |   "sources": [
            |       {"configurationId": "configuration1", "fetchIntervalInHours": "3", "metricsPrefix": "some.namespace", "sla": {"thresholdMinutes": 90}}
            |   ]
            |}
        """.trimMargin()
        assertThrows<SerializationException> {
            Json.decodeFromString<TeamcityMetricsSourceConfig>(configJson)
        }
    }

    @Test
    fun `sla with blank metricsPrefix - deserialization fails`() {
        val configJson = """
            |{
            |   "sources": [
            |       {"configurationId": "configuration1", "fetchIntervalInHours": "3", "metricsPrefix": "some.namespace", "sla": {"thresholdMinutes": 90, "metricsPrefix": " "}}
            |   ]
            |}
        """.trimMargin()
        assertThrows<IllegalArgumentException> {
            Json.decodeFromString<TeamcityMetricsSourceConfig>(configJson)
        }
    }

    @Test
    fun `sla with non-positive threshold - deserialization fails`() {
        val configJson = """
            |{
            |   "sources": [
            |       {"configurationId": "configuration1", "fetchIntervalInHours": "3", "metricsPrefix": "some.namespace", "sla": {"thresholdMinutes": 0, "metricsPrefix": "some.sla.speed"}}
            |   ]
            |}
        """.trimMargin()
        assertThrows<IllegalArgumentException> {
            Json.decodeFromString<TeamcityMetricsSourceConfig>(configJson)
        }
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
