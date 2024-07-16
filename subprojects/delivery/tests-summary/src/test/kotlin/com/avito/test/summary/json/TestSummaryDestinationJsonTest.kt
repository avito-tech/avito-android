package com.avito.test.summary.json

import com.avito.alertino.model.AlertinoRecipient
import com.avito.report.model.Team
import com.avito.test.summary.model.TestSummaryDestination
import com.github.salomonbrys.kotson.fromJson
import com.google.common.truth.Truth
import com.google.gson.GsonBuilder
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class TestSummaryDestinationJsonTest {

    @Language("json")
    private val json = """
        |[
        |{"teamName": "team1", "channel": "#channel1"},
        |{"teamName": "team2", "channel": "#channel2"}
        |]
    """.trimMargin()

    @Test
    fun deserialize(@TempDir dir: File) {
        val destination = File(dir, "test_summary_destination.json")
        destination.createNewFile()
        destination.writeText(json)

        val gson = GsonBuilder()
            .registerTypeAdapter(TestSummaryDestination::class.java, TestSummaryDestinationDeserializer())
            .create()

        val result = gson.fromJson<List<TestSummaryDestination>>(destination.reader())
        val assertThat = Truth.assertThat(result)
        assertThat.hasSize(2)
        assertThat.containsExactly(
            TestSummaryDestination(
                teamName = Team("team1"),
                channel = AlertinoRecipient("#channel1")
            ),
            TestSummaryDestination(
                teamName = Team("team2"),
                channel = AlertinoRecipient("#channel2")
            ),
        )
    }
}
