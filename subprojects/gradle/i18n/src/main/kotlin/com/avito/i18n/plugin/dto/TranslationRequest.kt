package com.avito.i18n.plugin.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class TranslationRequest(
    @SerialName("namespace") val namespace: String,
    @SerialName("sourceLang") val sourceLang: String,
    @SerialName("platform") val platform: String,
    @SerialName("targetLangs") val targetLangs: List<String>,
    @SerialName("texts") val textUnits: List<TranslationInputText>
)

@Serializable
internal data class TranslationInputText(
    @SerialName("key") val key: String,
    @SerialName("component") val componentName: String,
    @SerialName("text") val text: TranslationText,
    @SerialName("context") val context: TranslationContext?
)

@Serializable
internal data class TranslationContext(
    @SerialName("text") val text: String?
)
