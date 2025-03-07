package com.avito.i18n.plugin.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class TranslationResponse(
    @SerialName("result") val result: TranslationResult
)

@Serializable
internal data class TranslationResult(
    @SerialName("data") val data: TranslationData? = null,
    @SerialName("error") val error: Map<String, String>? = null
)

@Serializable
internal data class TranslationData(
    @SerialName("componentSlug") val componentSlug: String,
    @SerialName("namespaceSlug") val namespaceSlug: String,
    @SerialName("targetTextUnits") val targetTextUnits: Map<String, TextUnits>
)

@Serializable
internal data class TextUnits(
    @SerialName("textUnits") val textUnits: List<Map<String, String>>
)
