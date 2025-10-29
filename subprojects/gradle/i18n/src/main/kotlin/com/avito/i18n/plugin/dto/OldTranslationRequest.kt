package com.avito.i18n.plugin.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class OldTranslationRequest(
    @SerialName("namespaceSlug") val namespace: String,
    @SerialName("componentSlug") val componentName: String,
    @SerialName("sourceLang") val sourceLang: String,
    @SerialName("targetLangs") val targetLangs: List<String>,
    @SerialName("sourceTextUnits") val textUnits: List<Map<String, String>>
)
