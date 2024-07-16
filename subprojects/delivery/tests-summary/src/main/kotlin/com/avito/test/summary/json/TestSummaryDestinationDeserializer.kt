package com.avito.test.summary.json

import com.avito.alertino.model.AlertinoRecipient
import com.avito.report.model.Team
import com.avito.test.summary.model.TestSummaryDestination
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

internal class TestSummaryDestinationDeserializer : JsonDeserializer<TestSummaryDestination> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): TestSummaryDestination {
        val jsonObject = json.asJsonObject
        return TestSummaryDestination(
            teamName = Team(jsonObject.get("teamName").asString),
            channel = AlertinoRecipient(jsonObject.get("channel").asString)
        )
    }
}
