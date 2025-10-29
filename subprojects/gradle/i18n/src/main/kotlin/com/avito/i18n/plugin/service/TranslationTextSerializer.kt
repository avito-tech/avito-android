package com.avito.i18n.plugin.service

import com.avito.i18n.plugin.dto.TranslationText
import com.google.gson.JsonParseException
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

internal object TranslationTextSerializer : JsonContentPolymorphicSerializer<TranslationText>(TranslationText::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out TranslationText> {
        return when {
            "text" in element.jsonObject -> TranslationText.Text.serializer()
            "plural" in element.jsonObject -> TranslationText.Plural.serializer()
            else -> throw JsonParseException("No 'text' or 'plural' field in translation text")
        }
    }
}
